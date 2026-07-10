package com.platform.identityaccess.application;

import java.util.UUID;

public record RoleContext(
    UUID roleEntityId,
    UUID establishmentId
) {
}
