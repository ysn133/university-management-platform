package com.platform.usermanagement.superadmin.presentation.dto;

import java.util.UUID;

import com.platform.identityaccess.domain.AccountRoleType;

public record CreateSuperAdminResponse(
    UUID userAccountId,
    UUID establishmentId,
    AccountRoleType roleType
) {
}
