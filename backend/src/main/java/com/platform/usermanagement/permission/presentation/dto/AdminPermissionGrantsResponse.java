package com.platform.usermanagement.permission.presentation.dto;

import com.platform.identityaccess.domain.PermissionCode;
import java.util.List;
import java.util.UUID;

public record AdminPermissionGrantsResponse(
    UUID adminId,
    UUID establishmentId,
    List<PermissionCode> permissions
) {
}
