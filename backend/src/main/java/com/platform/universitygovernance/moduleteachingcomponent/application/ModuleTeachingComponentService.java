package com.platform.universitygovernance.moduleteachingcomponent.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.academicdomain.domain.AcademicDomain;
import com.platform.universitygovernance.academicdomain.infrastructure.AcademicDomainRepository;
import com.platform.universitygovernance.moduleteachingcomponent.domain.ModuleTeachingComponent;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentDomain;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import com.platform.universitygovernance.moduleteachingcomponent.infrastructure.ModuleTeachingComponentRepository;
import com.platform.universitygovernance.moduleteachingcomponent.infrastructure.TeachingComponentDomainRepository;
import com.platform.universitygovernance.moduleteachingcomponent.presentation.dto.ModuleTeachingComponentItemRequest;
import com.platform.universitygovernance.moduleteachingcomponent.presentation.dto.ModuleTeachingComponentResponse;
import com.platform.universitygovernance.moduleteachingcomponent.presentation.dto.ReplaceModuleTeachingComponentsRequest;
import com.platform.universitygovernance.subjectmodules.domain.SubjectModule;
import com.platform.universitygovernance.subjectmodules.infrastructure.SubjectModuleRepository;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
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
    private final TeachingComponentDomainRepository componentDomainRepository;
    private final AcademicDomainRepository academicDomainRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public ModuleTeachingComponentService(
        SubjectModuleRepository subjectModuleRepository,
        ModuleTeachingComponentRepository componentRepository,
        TeachingComponentDomainRepository componentDomainRepository,
        AcademicDomainRepository academicDomainRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.subjectModuleRepository = subjectModuleRepository;
        this.componentRepository = componentRepository;
        this.componentDomainRepository = componentDomainRepository;
        this.academicDomainRepository = academicDomainRepository;
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
        Map<UUID, AcademicDomain> academicDomains = loadAcademicDomains(
            request.components(),
            establishmentId
        );

        List<ModuleTeachingComponent> existingComponents =
            componentRepository.findBySubjectModuleIdOrderByComponentTypeAsc(subjectModuleId);
        List<UUID> existingIds = existingComponents.stream()
            .map(ModuleTeachingComponent::getId)
            .toList();
        if (!existingIds.isEmpty()) {
            componentDomainRepository.deleteAll(
                componentDomainRepository.findByModuleTeachingComponentIdIn(existingIds)
            );
            componentDomainRepository.flush();
        }

        Map<TeachingComponentType, ModuleTeachingComponent> existingByType =
            new EnumMap<>(TeachingComponentType.class);
        for (ModuleTeachingComponent component : existingComponents) {
            existingByType.put(component.getComponentType(), component);
        }

        List<ModuleTeachingComponent> configuredComponents = new ArrayList<>();
        Map<TeachingComponentType, ModuleTeachingComponentItemRequest> requestsByType =
            new EnumMap<>(TeachingComponentType.class);
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
            component.setMaximumGroupSize(item.maximumGroupSize());
            component.setRequiredRoomType(item.requiredRoomType());
            configuredComponents.add(component);
            requestsByType.put(item.componentType(), item);
        }

        componentRepository.deleteAll(existingByType.values());
        componentRepository.flush();
        configuredComponents = componentRepository.saveAll(configuredComponents);
        componentRepository.flush();

        List<TeachingComponentDomain> domainLinks = new ArrayList<>();
        for (ModuleTeachingComponent component : configuredComponents) {
            ModuleTeachingComponentItemRequest item = requestsByType.get(component.getComponentType());
            for (UUID domainId : item.requiredDomainIds()) {
                TeachingComponentDomain link = new TeachingComponentDomain();
                link.setModuleTeachingComponent(component);
                link.setAcademicDomain(academicDomains.get(domainId));
                domainLinks.add(link);
            }
        }
        componentDomainRepository.saveAll(domainLinks);
        componentDomainRepository.flush();
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
            if (component.audienceMode() == TeachingAudienceMode.SUBGROUP
                && component.maximumGroupSize() == null) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Subgroup teaching components require a maximum group size"
                );
            }
        }
    }

    private Map<UUID, AcademicDomain> loadAcademicDomains(
        List<ModuleTeachingComponentItemRequest> components,
        UUID establishmentId
    ) {
        Set<UUID> domainIds = new HashSet<>();
        for (ModuleTeachingComponentItemRequest component : components) {
            domainIds.addAll(component.requiredDomainIds());
        }
        List<AcademicDomain> domains = academicDomainRepository.findAllById(domainIds);
        if (domains.size() != domainIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Academic domain not found");
        }
        if (domains.stream().anyMatch(domain ->
            !establishmentId.equals(domain.getEstablishment().getId()))) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Academic domains must belong to the subject module's establishment"
            );
        }
        Map<UUID, AcademicDomain> domainsById = new HashMap<>();
        for (AcademicDomain domain : domains) {
            domainsById.put(domain.getId(), domain);
        }
        return domainsById;
    }

    private List<ModuleTeachingComponentResponse> toResponses(UUID subjectModuleId) {
        List<ModuleTeachingComponent> components =
            componentRepository.findBySubjectModuleIdOrderByComponentTypeAsc(subjectModuleId);
        if (components.isEmpty()) {
            return List.of();
        }

        List<UUID> componentIds = components.stream()
            .map(ModuleTeachingComponent::getId)
            .toList();
        Map<UUID, List<UUID>> domainIdsByComponent = new HashMap<>();
        for (TeachingComponentDomain link :
            componentDomainRepository.findByModuleTeachingComponentIdIn(componentIds)) {
            domainIdsByComponent
                .computeIfAbsent(link.getModuleTeachingComponent().getId(), ignored -> new ArrayList<>())
                .add(link.getAcademicDomain().getId());
        }

        return components.stream()
            .map(component -> {
                List<UUID> requiredDomainIds = new ArrayList<>(
                    domainIdsByComponent.getOrDefault(component.getId(), List.of())
                );
                requiredDomainIds.sort((left, right) -> left.toString().compareTo(right.toString()));
                return new ModuleTeachingComponentResponse(
                    component.getId(),
                    component.getSubjectModule().getId(),
                    component.getComponentType(),
                    component.getSessionsPerWeek(),
                    component.getSessionDurationMinutes(),
                    component.getAudienceMode(),
                    component.getMaximumGroupSize(),
                    component.getRequiredRoomType(),
                    requiredDomainIds,
                    component.getCreatedAt(),
                    component.getUpdatedAt()
                );
            })
            .toList();
    }
}
