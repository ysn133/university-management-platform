package com.platform.platform.infrastructure.security;

import com.platform.identityaccess.domain.AccountRoleType;
import java.util.UUID;

public record AuthenticatedUserPrincipal(
    UUID userAccountId,
    AccountRoleType role,
    UUID roleEntityId,
    UUID establishmentId,
    String universityEmail
) {
}
