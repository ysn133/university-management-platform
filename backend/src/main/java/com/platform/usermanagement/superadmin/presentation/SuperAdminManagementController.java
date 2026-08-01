package com.platform.usermanagement.superadmin.presentation;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.platform.shared.presentation.ActionResponse;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.usermanagement.superadmin.application.SuperAdminManagementService;
import com.platform.usermanagement.superadmin.presentation.dto.CreateSuperAdminRequest;
import com.platform.usermanagement.superadmin.presentation.dto.CreateSuperAdminResponse;
import com.platform.usermanagement.superadmin.presentation.dto.ResetPasswordRequest;
import com.platform.usermanagement.superadmin.presentation.dto.SuperAdminProfileResponse;
import com.platform.usermanagement.superadmin.presentation.dto.UpdateSuperAdminRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasRole('ROOT_SUPER_ADMIN')")
public class SuperAdminManagementController {

    private final SuperAdminManagementService superAdminManagementService;

    public SuperAdminManagementController(SuperAdminManagementService superAdminManagementService) {
        this.superAdminManagementService = superAdminManagementService;
    }

    @PostMapping("/establishments/{id}/super-admins")
    public CreateSuperAdminResponse createSuperAdmin(
        @Valid @RequestBody CreateSuperAdminRequest request,
        @PathVariable("id") UUID establishmentId
    ) {
        return superAdminManagementService.createSuperAdmin(request, establishmentId);
    }

    @PostMapping("/super-admins/{id}/password-reset")
    public ActionResponse resetPassword(
        @Valid @RequestBody ResetPasswordRequest request,
        @PathVariable("id") UUID superAdminId
    ) {
        return superAdminManagementService.resetPassword(request, superAdminId);
    }

    @PostMapping("/super-admins/{id}/lock")
    public ActionResponse lockAccount(@PathVariable("id") UUID superAdminId) {
        return superAdminManagementService.lockAccount(superAdminId);
    }

    @PostMapping("/super-admins/{id}/unlock")
    public ActionResponse unlockAccount(@PathVariable("id") UUID superAdminId) {
        return superAdminManagementService.unlockAccount(superAdminId);
    }

    @PostMapping("/super-admins/{id}/deactivate")
    public ActionResponse deactivateAccount(@PathVariable("id") UUID superAdminId) {
        return superAdminManagementService.deactivateAccount(superAdminId);
    }

    @PostMapping("/super-admins/{id}/archive")
    public ActionResponse archiveAccount(@PathVariable("id") UUID superAdminId) {
        return superAdminManagementService.archiveAccount(superAdminId);
    }


    @GetMapping("/super-admins/{superAdminId}")
    public SuperAdminProfileResponse getSuperAdmin(@PathVariable("superAdminId") UUID superAdminId){
        return superAdminManagementService.getSuperAdmin(superAdminId);
    }

    @PutMapping("/super-admins/{superAdminId}")
    public SuperAdminProfileResponse updateSuperAdmin(
        @PathVariable("superAdminId") UUID superAdminId,
        @Valid @RequestBody UpdateSuperAdminRequest request
    ) {
        return superAdminManagementService.updateSuperAdmin(superAdminId, request);
    }

    @GetMapping("/establishments/{establishmentId}/super-admins")
    public List<SuperAdminProfileResponse> getSuperAdmins(
        @PathVariable("establishmentId") UUID establishmentId,
        @RequestParam(required = false) String query,
        @RequestParam(required = false) AccountStatus status
    ) {
        return superAdminManagementService.getSuperAdmins(establishmentId, query, status);
    }
}
