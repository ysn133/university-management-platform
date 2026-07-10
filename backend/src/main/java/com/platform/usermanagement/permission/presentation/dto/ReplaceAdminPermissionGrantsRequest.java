package com.platform.usermanagement.permission.presentation.dto;

import com.platform.identityaccess.domain.PermissionCode;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record ReplaceAdminPermissionGrantsRequest(
    @NotNull Set<PermissionCode> permissions
) {
}
