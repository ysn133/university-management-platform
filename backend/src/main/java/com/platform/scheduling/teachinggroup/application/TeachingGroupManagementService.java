package com.platform.scheduling.teachinggroup.application;

import com.platform.academicregistration.semesterregistration.domain.SemesterRegistration;
import com.platform.academicregistration.moduleregistration.domain.ModuleRegistrationStatus;
import com.platform.academicregistration.moduleregistration.infrastructure.ModuleRegistrationRepository;
import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.UserProfile;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.teachinggroup.domain.TeachingGroup;
import com.platform.scheduling.teachinggroup.domain.TeachingGroupMembership;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupMembershipRepository;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupPolicyRepository;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupRepository;
import com.platform.scheduling.teachinggroup.presentation.dto.TeachingGroupMemberResponse;
import com.platform.scheduling.teachinggroup.presentation.dto.TeachingGroupResponse;
import com.platform.scheduling.teachinggroup.presentation.dto.TeachingGroupRosterResponse;
import com.platform.teachingrequirement.infrastructure.TeachingRequirementRepository;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode;
import com.platform.universitygovernance.semester.domain.Semester;
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TeachingGroupManagementService {

    private final SemesterRepository semesterRepository;
    private final TeachingGroupRepository groupRepository;
    private final TeachingGroupMembershipRepository membershipRepository;
    private final TeachingGroupPolicyRepository policyRepository;
    private final ModuleRegistrationRepository moduleRegistrationRepository;
    private final UserProfileRepository userProfileRepository;
    private final TeachingRequirementRepository requirementRepository;
    private final TeachingGroupGenerationService generationService;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public TeachingGroupManagementService(
        SemesterRepository semesterRepository,
        TeachingGroupRepository groupRepository,
        TeachingGroupMembershipRepository membershipRepository,
        TeachingGroupPolicyRepository policyRepository,
        ModuleRegistrationRepository moduleRegistrationRepository,
        UserProfileRepository userProfileRepository,
        TeachingRequirementRepository requirementRepository,
        TeachingGroupGenerationService generationService,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.semesterRepository = semesterRepository;
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.policyRepository = policyRepository;
        this.moduleRegistrationRepository = moduleRegistrationRepository;
        this.userProfileRepository = userProfileRepository;
        this.requirementRepository = requirementRepository;
        this.generationService = generationService;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional(readOnly = true)
    public TeachingGroupRosterResponse getRoster(
        AuthenticatedUserPrincipal principal,
        UUID semesterId
    ) {
        Semester semester = requireSemester(semesterId);
        requirePermission(principal, semester, PermissionCode.TEACHING_GROUP_VIEW);
        return buildRoster(semesterId);
    }

    @Transactional
    public TeachingGroupRosterResponse generate(
        AuthenticatedUserPrincipal principal,
        UUID semesterId
    ) {
        Semester semester = requireSemester(semesterId);
        requirePermission(principal, semester, PermissionCode.TEACHING_GROUP_GENERATE);
        generationService.generateForSemester(semesterId);
        return buildRoster(semesterId);
    }

    @Transactional
    public TeachingGroupRosterResponse moveMember(
        AuthenticatedUserPrincipal principal,
        UUID targetGroupId,
        UUID semesterRegistrationId
    ) {
        TeachingGroup target = groupRepository.findById(targetGroupId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Teaching group not found"
            ));
        if (target.getAudienceType() != TeachingAudienceMode.SUBGROUP) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Students can only be moved between TD or TP groups"
            );
        }
        requirePermission(
            principal,
            target.getSemester(),
            PermissionCode.TEACHING_GROUP_UPDATE
        );
        if (requirementRepository.existsByTeachingGroupSemesterId(
            target.getSemester().getId()
        )) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Teaching group memberships cannot change after requirements exist"
            );
        }

        List<TeachingGroupMembership> semesterMemberships = membershipRepository
            .findByTeachingGroupSemesterIdAndSemesterRegistrationId(
                target.getSemester().getId(),
                semesterRegistrationId
            );
        TeachingGroupMembership current = semesterMemberships.stream()
            .filter(membership -> membership.getTeachingGroup().getAudienceType()
                == TeachingAudienceMode.SUBGROUP)
            .filter(membership -> membership.getTeachingGroup().getGroupType()
                == target.getGroupType())
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Student is not assigned to this teaching group type"
            ));

        TeachingGroup source = current.getTeachingGroup();
        if (!source.getSemester().getId().equals(target.getSemester().getId())
            || !source.getSourceClassGroup().getId().equals(
                target.getSourceClassGroup().getId()
            )) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "The target group must belong to the same class group"
            );
        }
        if (!source.getId().equals(target.getId())) {
            var policy = policyRepository
                .findByAcademicLevelIdAndAcademicYearIdOrderByGroupTypeAsc(
                    target.getSemester().getAcademicLevel().getId(),
                    target.getSemester().getAcademicYear().getId()
                )
                .stream()
                .filter(candidate -> candidate.getGroupType() == target.getGroupType())
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Teaching group policy not found"
                ));
            int sourceSize = membershipRepository.findByTeachingGroupId(source.getId()).size();
            int targetSize = membershipRepository.findByTeachingGroupId(target.getId()).size();
            if (sourceSize - 1 < policy.getMinimumGroupSize()) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The source group cannot fall below its minimum size"
                );
            }
            if (targetSize + 1 > policy.getMaximumGroupSize()) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The target group cannot exceed its maximum size"
                );
            }
            current.setTeachingGroup(target);
            membershipRepository.save(current);
        }
        return buildRoster(target.getSemester().getId());
    }

    private TeachingGroupRosterResponse buildRoster(UUID semesterId) {
        List<TeachingGroup> groups = groupRepository
            .findBySemesterIdOrderByAudienceTypeAscNameAsc(semesterId)
            .stream()
            .filter(group -> group.getAudienceType() == TeachingAudienceMode.SUBGROUP)
            .toList();
        Map<UUID, List<TeachingGroupMembership>> membershipsByGroup = groups.isEmpty()
            ? Map.of()
            : membershipRepository
                .findByTeachingGroupIdIn(groups.stream().map(TeachingGroup::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(membership ->
                    membership.getTeachingGroup().getId()));

        List<TeachingGroupResponse> responses = groups.stream()
            .map(group -> toResponse(group, membershipsByGroup.getOrDefault(
                group.getId(),
                List.of()
            )))
            .toList();
        return new TeachingGroupRosterResponse(semesterId, responses);
    }

    private TeachingGroupResponse toResponse(
        TeachingGroup group,
        List<TeachingGroupMembership> memberships
    ) {
        List<SemesterRegistration> registrations = memberships.stream()
            .map(TeachingGroupMembership::getSemesterRegistration)
            .toList();
        Map<UUID, UserProfile> profilesByAccount = registrations.stream()
            .map(registration -> registration.getAcademicRegistration().getStudent()
                .getUserAccount().getId())
            .distinct()
            .map(userProfileRepository::findByUserAccountId)
            .flatMap(java.util.Optional::stream)
            .collect(Collectors.toMap(
                profile -> profile.getUserAccount().getId(),
                Function.identity()
            ));
        List<TeachingGroupMemberResponse> members = registrations.stream()
            .map(registration -> {
                var student = registration.getAcademicRegistration().getStudent();
                UserProfile profile = profilesByAccount.get(student.getUserAccount().getId());
                return new TeachingGroupMemberResponse(
                    registration.getId(),
                    student.getId(),
                    student.getApogeeCode(),
                    profile == null ? "" : profile.getFirstName(),
                    profile == null ? "" : profile.getLastName(),
                    moduleRegistrationRepository
                        .findBySemesterRegistrationIdAndStatus(
                            registration.getId(),
                            ModuleRegistrationStatus.ACTIVE
                        )
                        .stream()
                        .anyMatch(moduleRegistration ->
                            moduleRegistration.getInscriptionNumber() > 1)
                );
            })
            .sorted(Comparator.comparing(TeachingGroupMemberResponse::apogeeCode))
            .toList();
        return new TeachingGroupResponse(
            group.getId(),
            group.getSemester().getId(),
            group.getSourceClassGroup().getId(),
            group.getSourceClassGroup().getName(),
            group.getName(),
            group.getGroupType(),
            members
        );
    }

    private Semester requireSemester(UUID semesterId) {
        return semesterRepository.findById(semesterId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Semester not found"
            ));
    }

    private void requirePermission(
        AuthenticatedUserPrincipal principal,
        Semester semester,
        PermissionCode permissionCode
    ) {
        UUID establishmentId = semester.getAcademicLevel().getProgramFiliere()
            .getDepartment().getEstablishment().getId();
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            permissionCode
        );
    }
}
