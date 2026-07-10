package com.platform.usermanagement.admin.presentation.dto;

import java.util.UUID;

import com.platform.identityaccess.domain.AccountRoleType;

public record CreateAdminResponse(
    UUID adminId,
    UUID userAccountId,
    UUID establishmentId,
    AccountRoleType roleType
) {
}
