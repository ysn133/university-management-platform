package com.platform.usermanagement.permission.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.usermanagement.permission.application.AdminPermissionManagementService;
import com.platform.usermanagement.permission.presentation.dto.AdminPermissionGrantsResponse;
import com.platform.usermanagement.permission.presentation.dto.PermissionResponse;
import com.platform.usermanagement.permission.presentation.dto.ReplaceAdminPermissionGrantsRequest;
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
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN')")
public class AdminPermissionManagementController {

    private final AdminPermissionManagementService adminPermissionManagementService;

    public AdminPermissionManagementController(
        AdminPermissionManagementService adminPermissionManagementService
    ) {
        this.adminPermissionManagementService = adminPermissionManagementService;
    }

    @GetMapping("/permissions")
    public List<PermissionResponse> getPermissionCatalog() {
        return adminPermissionManagementService.getPermissionCatalog();
    }

    @GetMapping("/admins/{id}/permission-grants")
    public AdminPermissionGrantsResponse getAdminGrants(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable("id") UUID adminId
    ) {
        return adminPermissionManagementService.getAdminGrants(principal, adminId);
    }

    @PutMapping("/admins/{id}/permission-grants")
    public AdminPermissionGrantsResponse replaceAdminGrants(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable("id") UUID adminId,
        @Valid @RequestBody ReplaceAdminPermissionGrantsRequest request
    ) {
        return adminPermissionManagementService.replaceAdminGrants(principal, adminId, request);
    }
}
