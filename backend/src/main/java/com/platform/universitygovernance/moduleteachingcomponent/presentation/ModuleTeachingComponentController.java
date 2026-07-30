package com.platform.universitygovernance.moduleteachingcomponent.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.moduleteachingcomponent.application.ModuleTeachingComponentService;
import com.platform.universitygovernance.moduleteachingcomponent.presentation.dto.ModuleTeachingComponentResponse;
import com.platform.universitygovernance.moduleteachingcomponent.presentation.dto.ReplaceModuleTeachingComponentsRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subject-modules/{subjectModuleId}/teaching-components")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
public class ModuleTeachingComponentController {

    private final ModuleTeachingComponentService moduleTeachingComponentService;

    public ModuleTeachingComponentController(
        ModuleTeachingComponentService moduleTeachingComponentService
    ) {
        this.moduleTeachingComponentService = moduleTeachingComponentService;
    }

    @GetMapping
    public List<ModuleTeachingComponentResponse> getModuleTeachingComponents(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID subjectModuleId
    ) {
        return moduleTeachingComponentService.getModuleTeachingComponents(
            principal,
            subjectModuleId
        );
    }

    @PutMapping
    public List<ModuleTeachingComponentResponse> replaceModuleTeachingComponents(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID subjectModuleId,
        @Valid @RequestBody ReplaceModuleTeachingComponentsRequest request
    ) {
        return moduleTeachingComponentService.replaceModuleTeachingComponents(
            principal,
            subjectModuleId,
            request
        );
    }
}
