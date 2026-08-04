package com.platform.usermanagement.admin.presentation;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.usermanagement.admin.application.AdminManagementService;
import com.platform.usermanagement.admin.presentation.dto.AdminProfileResponse;
import com.platform.usermanagement.admin.presentation.dto.CreateAdminRequest;
import com.platform.usermanagement.admin.presentation.dto.CreateAdminResponse;
import com.platform.usermanagement.admin.presentation.dto.ResetAdminPasswordRequest;
import com.platform.usermanagement.admin.presentation.dto.UpdateAdminRequest;
import com.platform.identityaccess.domain.AccountStatus;
import java.time.LocalDate;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class AdminManagementController {

    private final AdminManagementService adminManagementService;

    public AdminManagementController(AdminManagementService adminManagementService) {
        this.adminManagementService = adminManagementService;
    }

    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    @PostMapping("/establishments/{id}/admins")
    public CreateAdminResponse createAdmin(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @Valid @RequestBody CreateAdminRequest request,
        @PathVariable("id") UUID establishmentId
    ) {
        return adminManagementService.createAdmin(principal, request, establishmentId);
    }

    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/establishments/{id}/admins")
    public List<AdminProfileResponse> getAdmins(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable("id") UUID establishmentId,
        @RequestParam(required = false) String query,
        @RequestParam(required = false) AccountStatus status,
        @RequestParam(required = false) LocalDate createdFrom,
        @RequestParam(required = false) LocalDate createdTo
    ) {
        return adminManagementService.getAdmins(
            principal, establishmentId, query, status, createdFrom, createdTo
        );
    }

    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/admins/{id}")
    public AdminProfileResponse updateAdmin(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable("id") UUID adminId,
        @Valid @RequestBody UpdateAdminRequest request
    ) {
        return adminManagementService.updateAdmin(principal, adminId, request);
    }

    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/admins/{id}")
    public AdminProfileResponse getAdmin(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable("id") UUID adminId
    ) {
        return adminManagementService.getAdmin(principal, adminId);
    }

    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/admins/{id}/password-reset")
    public ActionResponse resetPassword(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable("id") UUID adminId,
        @Valid @RequestBody ResetAdminPasswordRequest request
    ) {
        return adminManagementService.resetPassword(principal, adminId, request);
    }

    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/admins/{id}/lock")
    public ActionResponse lockAccount(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable("id") UUID adminId
    ) {
        return adminManagementService.lockAccount(principal, adminId);
    }

    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/admins/{id}/unlock")
    public ActionResponse unlockAccount(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable("id") UUID adminId
    ) {
        return adminManagementService.unlockAccount(principal, adminId);
    }

    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/admins/{id}/deactivate")
    public ActionResponse deactivateAccount(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable("id") UUID adminId
    ) {
        return adminManagementService.deactivateAccount(principal, adminId);
    }

    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/admins/{id}/activate")
    public ActionResponse activateAccount(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable("id") UUID adminId
    ) {
        return adminManagementService.activateAccount(principal, adminId);
    }

    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/admins/{id}/archive")
    public ActionResponse archiveAccount(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable("id") UUID adminId
    ) {
        return adminManagementService.archiveAccount(principal, adminId);
    }

    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/admins/{id}/restore")
    public ActionResponse restoreAccount(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable("id") UUID adminId
    ) {
        return adminManagementService.restoreAccount(principal, adminId);
    }
}
