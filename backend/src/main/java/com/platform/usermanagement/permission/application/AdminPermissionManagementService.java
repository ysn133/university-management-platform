package com.platform.usermanagement.permission.application;

import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.Admin;
import com.platform.identityaccess.domain.AdminPermissionGrant;
import com.platform.identityaccess.domain.Permission;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.infrastructure.AdminPermissionGrantRepository;
import com.platform.identityaccess.infrastructure.AdminRepository;
import com.platform.identityaccess.infrastructure.PermissionRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.usermanagement.permission.presentation.dto.AdminPermissionGrantsResponse;
import com.platform.usermanagement.permission.presentation.dto.PermissionResponse;
import com.platform.usermanagement.permission.presentation.dto.ReplaceAdminPermissionGrantsRequest;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminPermissionManagementService {

    private final PermissionRepository permissionRepository;
    private final AdminPermissionGrantRepository adminPermissionGrantRepository;
    private final AdminRepository adminRepository;

    public AdminPermissionManagementService(
        PermissionRepository permissionRepository,
        AdminPermissionGrantRepository adminPermissionGrantRepository,
        AdminRepository adminRepository
    ) {
        this.permissionRepository = permissionRepository;
        this.adminPermissionGrantRepository = adminPermissionGrantRepository;
        this.adminRepository = adminRepository;
    }

    @Transactional
    public List<PermissionResponse> getPermissionCatalog() {
        return permissionRepository.findAllByOrderByCodeAsc().stream()
            .map(permission -> new PermissionResponse(
                permission.getId(),
                permission.getCode(),
                permission.getName()
            ))
            .toList();
    }

    @Transactional
    public AdminPermissionGrantsResponse getAdminGrants(
        AuthenticatedUserPrincipal principal,
        UUID adminId
    ) {
        Admin admin = findAdmin(adminId);
        ensureCallerCanManageGrants(principal, admin.getEstablishment().getId());
        return buildResponse(admin);
    }

    @Transactional
    public AdminPermissionGrantsResponse replaceAdminGrants(
        AuthenticatedUserPrincipal principal,
        UUID adminId,
        ReplaceAdminPermissionGrantsRequest request
    ) {
        Admin admin = findAdmin(adminId);
        ensureCallerCanManageGrants(principal, admin.getEstablishment().getId());

        List<Permission> permissions = permissionRepository.findByCodeIn(request.permissions());
        if (permissions.size() != request.permissions().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown permission code");
        }

        adminPermissionGrantRepository.deleteByAdminId(adminId);
        adminPermissionGrantRepository.flush();

        List<AdminPermissionGrant> grants = new ArrayList<>();
        for (Permission permission : permissions) {
            AdminPermissionGrant grant = new AdminPermissionGrant();
            grant.setAdmin(admin);
            grant.setPermission(permission);
            grants.add(grant);
        }
        adminPermissionGrantRepository.saveAll(grants);

        return buildResponse(admin);
    }

    private AdminPermissionGrantsResponse buildResponse(Admin admin) {
        List<PermissionCode> permissions = adminPermissionGrantRepository.findByAdminId(admin.getId()).stream()
            .map(grant -> grant.getPermission().getCode())
            .sorted(Comparator.comparing(Enum::name))
            .toList();

        return new AdminPermissionGrantsResponse(
            admin.getId(),
            admin.getEstablishment().getId(),
            permissions
        );
    }

    private Admin findAdmin(UUID adminId) {
        return adminRepository.findById(adminId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
    }

    private void ensureCallerCanManageGrants(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        if (principal.role() == AccountRoleType.ROOT_SUPER_ADMIN) {
            return;
        }

        if (principal.role() == AccountRoleType.SUPER_ADMIN
            && establishmentId.equals(principal.establishmentId())) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot manage these permission grants");
    }
}
