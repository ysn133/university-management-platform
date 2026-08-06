package com.platform.scheduling.teachinggroup.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.teachinggroup.domain.TeachingGroupPolicy;
import com.platform.scheduling.teachinggroup.domain.TeachingGroupType;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupPolicyRepository;
import com.platform.scheduling.teachinggroup.presentation.dto.ReplaceTeachingGroupPoliciesRequest;
import com.platform.scheduling.teachinggroup.presentation.dto.TeachingGroupPolicyItemRequest;
import com.platform.scheduling.teachinggroup.presentation.dto.TeachingGroupPolicyResponse;
import com.platform.teachingrequirement.infrastructure.TeachingRequirementRepository;
import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academiclevel.infrastructure.AcademicLevelRepository;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TeachingGroupPolicyService {

    private final AcademicLevelRepository academicLevelRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;
    private final TeachingGroupPolicyRepository policyRepository;
    private final TeachingRequirementRepository requirementRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public TeachingGroupPolicyService(
        AcademicLevelRepository academicLevelRepository,
        AcademicYearRepository academicYearRepository,
        SemesterRepository semesterRepository,
        TeachingGroupPolicyRepository policyRepository,
        TeachingRequirementRepository requirementRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.academicLevelRepository = academicLevelRepository;
        this.academicYearRepository = academicYearRepository;
        this.semesterRepository = semesterRepository;
        this.policyRepository = policyRepository;
        this.requirementRepository = requirementRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional(readOnly = true)
    public List<TeachingGroupPolicyResponse> getPolicies(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId,
        UUID academicYearId
    ) {
        AcademicLevel academicLevel = findAcademicLevel(academicLevelId);
        AcademicYear academicYear = findAcademicYear(academicYearId);
        UUID establishmentId = validateContext(academicLevel, academicYear);
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.TEACHING_GROUP_POLICY_VIEW
        );
        return toResponses(academicLevelId, academicYearId);
    }

    @Transactional
    public List<TeachingGroupPolicyResponse> replacePolicies(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId,
        UUID academicYearId,
        ReplaceTeachingGroupPoliciesRequest request
    ) {
        AcademicLevel academicLevel = findAcademicLevel(academicLevelId);
        AcademicYear academicYear = findAcademicYear(academicYearId);
        UUID establishmentId = validateContext(academicLevel, academicYear);
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.TEACHING_GROUP_POLICY_UPDATE
        );
        validatePolicies(request.policies());
        ensureRequirementsDoNotExist(academicLevelId, academicYearId);

        List<TeachingGroupPolicy> existing = policyRepository
            .findByAcademicLevelIdAndAcademicYearIdOrderByGroupTypeAsc(
                academicLevelId,
                academicYearId
            );
        Map<TeachingGroupType, TeachingGroupPolicy> existingByType =
            new EnumMap<>(TeachingGroupType.class);
        existing.forEach(policy -> existingByType.put(policy.getGroupType(), policy));

        for (TeachingGroupPolicyItemRequest item : request.policies()) {
            TeachingGroupPolicy policy = existingByType.remove(item.groupType());
            if (policy == null) {
                policy = new TeachingGroupPolicy();
                policy.setAcademicLevel(academicLevel);
                policy.setAcademicYear(academicYear);
                policy.setGroupType(item.groupType());
            }
            policy.setMinimumGroupSize(item.minimumGroupSize());
            policy.setMaximumGroupSize(item.maximumGroupSize());
            policyRepository.save(policy);
        }
        policyRepository.deleteAll(existingByType.values());
        policyRepository.flush();
        return toResponses(academicLevelId, academicYearId);
    }

    private void validatePolicies(List<TeachingGroupPolicyItemRequest> policies) {
        Set<TeachingGroupType> types = new HashSet<>();
        for (TeachingGroupPolicyItemRequest policy : policies) {
            if (policy.minimumGroupSize() > policy.maximumGroupSize()) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum group size cannot exceed maximum group size"
                );
            }
            if (!types.add(policy.groupType())) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Each teaching group policy type may appear only once"
                );
            }
        }
    }

    private void ensureRequirementsDoNotExist(UUID academicLevelId, UUID academicYearId) {
        boolean requirementsExist = semesterRepository
            .findByAcademicLevelIdAndAcademicYearIdOrderBySemesterOrderAsc(
                academicLevelId,
                academicYearId
            )
            .stream()
            .anyMatch(semester -> requirementRepository
                .existsByTeachingGroupSemesterId(semester.getId()));
        if (requirementsExist) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Teaching group policies cannot change after teaching requirements exist"
            );
        }
    }

    private AcademicLevel findAcademicLevel(UUID academicLevelId) {
        return academicLevelRepository.findById(academicLevelId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic level not found"
            ));
    }

    private AcademicYear findAcademicYear(UUID academicYearId) {
        return academicYearRepository.findById(academicYearId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic year not found"
            ));
    }

    private UUID validateContext(AcademicLevel academicLevel, AcademicYear academicYear) {
        UUID establishmentId = academicLevel.getProgramFiliere()
            .getDepartment()
            .getEstablishment()
            .getId();
        if (!establishmentId.equals(academicYear.getEstablishment().getId())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Academic level and academic year must belong to the same establishment"
            );
        }
        return establishmentId;
    }

    private List<TeachingGroupPolicyResponse> toResponses(
        UUID academicLevelId,
        UUID academicYearId
    ) {
        return policyRepository
            .findByAcademicLevelIdAndAcademicYearIdOrderByGroupTypeAsc(
                academicLevelId,
                academicYearId
            )
            .stream()
            .map(policy -> new TeachingGroupPolicyResponse(
                policy.getId(),
                policy.getAcademicLevel().getId(),
                policy.getAcademicYear().getId(),
                policy.getGroupType(),
                policy.getMinimumGroupSize(),
                policy.getMaximumGroupSize(),
                policy.getCreatedAt(),
                policy.getUpdatedAt()
            ))
            .toList();
    }
}
