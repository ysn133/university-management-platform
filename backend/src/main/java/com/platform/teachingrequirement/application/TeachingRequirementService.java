package com.platform.teachingrequirement.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.teachinggroup.domain.TeachingGroup;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupRepository;
import com.platform.teachingrequirement.domain.TeachingRequirement;
import com.platform.teachingrequirement.domain.TeachingRequirementStatus;
import com.platform.teachingrequirement.infrastructure.TeachingRequirementRepository;
import com.platform.teachingrequirement.presentation.dto.TeachingRequirementResponse;
import com.platform.universitygovernance.moduleteachingcomponent.domain.ModuleTeachingComponent;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode;
import com.platform.universitygovernance.moduleteachingcomponent.infrastructure.ModuleTeachingComponentRepository;
import com.platform.universitygovernance.semester.domain.Semester;
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TeachingRequirementService {

    private final SemesterRepository semesterRepository;
    private final ModuleTeachingComponentRepository componentRepository;
    private final TeachingGroupRepository teachingGroupRepository;
    private final TeachingRequirementRepository requirementRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public TeachingRequirementService(
        SemesterRepository semesterRepository,
        ModuleTeachingComponentRepository componentRepository,
        TeachingGroupRepository teachingGroupRepository,
        TeachingRequirementRepository requirementRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.semesterRepository = semesterRepository;
        this.componentRepository = componentRepository;
        this.teachingGroupRepository = teachingGroupRepository;
        this.requirementRepository = requirementRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public List<TeachingRequirementResponse> generateForSemester(
        AuthenticatedUserPrincipal principal,
        UUID semesterId
    ) {
        Semester semester = findSemester(semesterId);
        UUID establishmentId = establishmentId(semester);
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.TEACHING_REQUIREMENT_GENERATE
        );

        List<ModuleTeachingComponent> components = componentRepository
            .findBySubjectModuleSemesterId(semesterId);
        List<TeachingGroup> groups = teachingGroupRepository
            .findBySemesterIdOrderByAudienceTypeAscNameAsc(semesterId);
        if (!components.isEmpty() && groups.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Teaching groups must be generated before teaching requirements"
            );
        }

        List<TeachingRequirement> existing = requirementRepository
            .findByTeachingGroupSemesterIdOrderByCreatedAtAsc(semesterId);
        Set<String> desiredKeys = new HashSet<>();
        List<TeachingRequirement> changed = new ArrayList<>();

        for (ModuleTeachingComponent component : components) {
            List<TeachingGroup> matchingGroups = groups.stream()
                .filter(group -> group.getAudienceType() == component.getAudienceMode())
                .filter(group -> component.getAudienceMode() != TeachingAudienceMode.SUBGROUP
                    || group.getGroupType().name().equals(component.getComponentType().name()))
                .toList();
            if (matchingGroups.isEmpty()) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No teaching group matches component " + component.getComponentType()
                );
            }
            for (TeachingGroup group : matchingGroups) {
                String key = key(component.getId(), group.getId());
                desiredKeys.add(key);
                TeachingRequirement requirement = existing.stream()
                    .filter(item -> key(
                        item.getModuleTeachingComponent().getId(),
                        item.getTeachingGroup().getId()
                    ).equals(key))
                    .findFirst()
                    .orElseGet(TeachingRequirement::new);
                requirement.setModuleTeachingComponent(component);
                requirement.setTeachingGroup(group);
                requirement.setStatus(TeachingRequirementStatus.ACTIVE);
                changed.add(requirement);
            }
        }

        existing.stream()
            .filter(requirement -> !desiredKeys.contains(key(
                requirement.getModuleTeachingComponent().getId(),
                requirement.getTeachingGroup().getId()
            )))
            .forEach(requirement -> {
                requirement.setStatus(TeachingRequirementStatus.INACTIVE);
                changed.add(requirement);
            });

        requirementRepository.saveAll(changed);
        requirementRepository.flush();
        return listResponses(semesterId);
    }

    @Transactional(readOnly = true)
    public List<TeachingRequirementResponse> getForSemester(
        AuthenticatedUserPrincipal principal,
        UUID semesterId
    ) {
        Semester semester = findSemester(semesterId);
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId(semester),
            PermissionCode.TEACHING_REQUIREMENT_VIEW
        );
        return listResponses(semesterId);
    }

    private List<TeachingRequirementResponse> listResponses(UUID semesterId) {
        return requirementRepository
            .findByTeachingGroupSemesterIdOrderByCreatedAtAsc(semesterId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private Semester findSemester(UUID semesterId) {
        return semesterRepository.findById(semesterId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Semester not found"
            ));
    }

    private UUID establishmentId(Semester semester) {
        return semester.getAcademicLevel()
            .getProgramFiliere()
            .getDepartment()
            .getEstablishment()
            .getId();
    }

    private String key(UUID componentId, UUID groupId) {
        return componentId + ":" + groupId;
    }

    private TeachingRequirementResponse toResponse(TeachingRequirement requirement) {
        ModuleTeachingComponent component = requirement.getModuleTeachingComponent();
        TeachingGroup group = requirement.getTeachingGroup();
        return new TeachingRequirementResponse(
            requirement.getId(),
            component.getSubjectModule().getId(),
            component.getId(),
            component.getComponentType(),
            group.getId(),
            group.getName(),
            group.getSourceClassGroup() == null
                ? null
                : group.getSourceClassGroup().getId(),
            group.getSourceClassGroup() == null
                ? null
                : group.getSourceClassGroup().getName(),
            group.getAudienceType(),
            requirement.getStatus()
        );
    }
}
