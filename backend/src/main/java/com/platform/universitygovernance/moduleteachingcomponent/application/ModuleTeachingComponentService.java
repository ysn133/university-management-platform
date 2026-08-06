package com.platform.universitygovernance.moduleteachingcomponent.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.moduleteachingcomponent.domain.ModuleTeachingComponent;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import com.platform.universitygovernance.moduleteachingcomponent.infrastructure.ModuleTeachingComponentRepository;
import com.platform.universitygovernance.moduleteachingcomponent.presentation.dto.ModuleTeachingComponentItemRequest;
import com.platform.universitygovernance.moduleteachingcomponent.presentation.dto.ModuleTeachingComponentResponse;
import com.platform.universitygovernance.moduleteachingcomponent.presentation.dto.ReplaceModuleTeachingComponentsRequest;
import com.platform.universitygovernance.subjectmodules.domain.SubjectModule;
import com.platform.universitygovernance.subjectmodules.infrastructure.SubjectModuleRepository;
import java.util.ArrayList;
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
public class ModuleTeachingComponentService {

    private final SubjectModuleRepository subjectModuleRepository;
    private final ModuleTeachingComponentRepository componentRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public ModuleTeachingComponentService(
        SubjectModuleRepository subjectModuleRepository,
        ModuleTeachingComponentRepository componentRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.subjectModuleRepository = subjectModuleRepository;
        this.componentRepository = componentRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional(readOnly = true)
    public List<ModuleTeachingComponentResponse> getModuleTeachingComponents(
        AuthenticatedUserPrincipal principal,
        UUID subjectModuleId
    ) {
        SubjectModule subjectModule = findSubjectModule(subjectModuleId);
        UUID establishmentId = establishmentId(subjectModule);
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.MODULE_TEACHING_COMPONENT_VIEW
        );
        return toResponses(subjectModuleId);
    }

    @Transactional
    public List<ModuleTeachingComponentResponse> replaceModuleTeachingComponents(
        AuthenticatedUserPrincipal principal,
        UUID subjectModuleId,
        ReplaceModuleTeachingComponentsRequest request
    ) {
        SubjectModule subjectModule = findSubjectModule(subjectModuleId);
        UUID establishmentId = establishmentId(subjectModule);
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.MODULE_TEACHING_COMPONENT_UPDATE
        );

        validateComponents(request.components());
        List<ModuleTeachingComponent> existingComponents =
            componentRepository.findBySubjectModuleIdOrderByComponentTypeAsc(subjectModuleId);

        Map<TeachingComponentType, ModuleTeachingComponent> existingByType =
            new EnumMap<>(TeachingComponentType.class);
        for (ModuleTeachingComponent component : existingComponents) {
            existingByType.put(component.getComponentType(), component);
        }

        List<ModuleTeachingComponent> configuredComponents = new ArrayList<>();
        for (ModuleTeachingComponentItemRequest item : request.components()) {
            ModuleTeachingComponent component = existingByType.remove(item.componentType());
            if (component == null) {
                component = new ModuleTeachingComponent();
                component.setSubjectModule(subjectModule);
                component.setComponentType(item.componentType());
            }
            component.setSessionsPerWeek(item.sessionsPerWeek());
            component.setSessionDurationMinutes(item.sessionDurationMinutes());
            component.setAudienceMode(item.audienceMode());
            component.setRequiredRoomType(item.requiredRoomType());
            configuredComponents.add(component);
        }

        componentRepository.deleteAll(existingByType.values());
        componentRepository.flush();
        configuredComponents = componentRepository.saveAll(configuredComponents);
        componentRepository.flush();
        return toResponses(subjectModuleId);
    }

    private SubjectModule findSubjectModule(UUID subjectModuleId) {
        return subjectModuleRepository.findById(subjectModuleId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Subject module not found"
            ));
    }

    private UUID establishmentId(SubjectModule subjectModule) {
        return subjectModule.getSemester().getAcademicYear().getEstablishment().getId();
    }

    private void validateComponents(List<ModuleTeachingComponentItemRequest> components) {
        Set<TeachingComponentType> types = new HashSet<>();
        for (ModuleTeachingComponentItemRequest component : components) {
            if (!types.add(component.componentType())) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Each teaching component type may appear only once"
                );
            }
            if (component.componentType() == TeachingComponentType.COURSE
                && component.audienceMode() == TeachingAudienceMode.SUBGROUP) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Course components cannot use TD or TP subgroups"
                );
            }
        }
    }

    private List<ModuleTeachingComponentResponse> toResponses(UUID subjectModuleId) {
        List<ModuleTeachingComponent> components =
            componentRepository.findBySubjectModuleIdOrderByComponentTypeAsc(subjectModuleId);
        if (components.isEmpty()) {
            return List.of();
        }

        return components.stream()
            .map(component -> new ModuleTeachingComponentResponse(
                    component.getId(),
                    component.getSubjectModule().getId(),
                    component.getComponentType(),
                    component.getSessionsPerWeek(),
                    component.getSessionDurationMinutes(),
                    component.getAudienceMode(),
                    component.getRequiredRoomType(),
                    component.getCreatedAt(),
                    component.getUpdatedAt()
                ))
            .toList();
    }
}
