package com.platform.moduleclassresponsibility.presentation;

import com.platform.moduleclassresponsibility.application.ModuleClassResponsibilityService;
import com.platform.moduleclassresponsibility.presentation.dto.CreateModuleClassResponsibilityRequest;
import com.platform.moduleclassresponsibility.presentation.dto.ModuleClassResponsibilityResponse;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ModuleClassResponsibilityController {

    private final ModuleClassResponsibilityService responsibilityService;

    public ModuleClassResponsibilityController(
        ModuleClassResponsibilityService responsibilityService
    ) {
        this.responsibilityService = responsibilityService;
    }

    @PostMapping("/establishments/{establishmentId}/module-class-responsibilities")
    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    public ModuleClassResponsibilityResponse createResponsibility(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId,
        @Valid @RequestBody CreateModuleClassResponsibilityRequest request
    ) {
        return responsibilityService.createResponsibility(
            principal,
            establishmentId,
            request
        );
    }

    @GetMapping("/establishments/{establishmentId}/module-class-responsibilities")
    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    public List<ModuleClassResponsibilityResponse> getResponsibilities(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId
    ) {
        return responsibilityService.getResponsibilities(
            principal,
            establishmentId
        );
    }

    @GetMapping("/module-class-responsibilities/{responsibilityId}")
    @PreAuthorize(
        "hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'PROFESSOR')"
    )
    public ModuleClassResponsibilityResponse getResponsibility(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID responsibilityId
    ) {
        return responsibilityService.getResponsibility(
            principal,
            responsibilityId
        );
    }

    @GetMapping("/me/module-class-responsibilities")
    @PreAuthorize("hasRole('PROFESSOR')")
    public List<ModuleClassResponsibilityResponse> getMyResponsibilities(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return responsibilityService.getMyResponsibilities(principal);
    }

    @DeleteMapping("/module-class-responsibilities/{responsibilityId}")
    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    public ActionResponse removeResponsibility(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID responsibilityId
    ) {
        return responsibilityService.removeResponsibility(
            principal,
            responsibilityId
        );
    }
}
