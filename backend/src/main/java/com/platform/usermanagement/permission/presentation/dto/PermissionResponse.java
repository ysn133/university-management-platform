package com.platform.usermanagement.permission.presentation.dto;

import com.platform.identityaccess.domain.PermissionCode;
import java.util.UUID;

public record PermissionResponse(
    UUID id,
    PermissionCode code,
    String name
) {
}
