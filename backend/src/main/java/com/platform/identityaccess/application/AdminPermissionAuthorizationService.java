package com.platform.identityaccess.application;

import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.infrastructure.AdminPermissionGrantRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminPermissionAuthorizationService {

    private final AdminPermissionGrantRepository adminPermissionGrantRepository;

    public AdminPermissionAuthorizationService(
        AdminPermissionGrantRepository adminPermissionGrantRepository
    ) {
        this.adminPermissionGrantRepository = adminPermissionGrantRepository;
    }

    public void requirePermission(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        PermissionCode permissionCode
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        if (principal.role() == AccountRoleType.ROOT_SUPER_ADMIN) {
            return;
        }

        if (!establishmentId.equals(principal.establishmentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for this establishment");
        }

        if (principal.role() == AccountRoleType.SUPER_ADMIN) {
            return;
        }

        if (principal.role() == AccountRoleType.ADMIN
            && adminPermissionGrantRepository.existsByAdminIdAndPermissionCode(
                principal.roleEntityId(),
                permissionCode
            )) {
            return;
        }

        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "Required permission: " + permissionCode.name()
        );
    }
}
