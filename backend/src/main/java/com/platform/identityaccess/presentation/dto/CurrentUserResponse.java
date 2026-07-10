package com.platform.identityaccess.presentation.dto;

import java.util.UUID;

public record CurrentUserResponse(
    UUID userAccountId,
    String role,
    UUID roleEntityId,
    UUID establishmentId,
    String universityEmail,
    String firstName,
    String lastName,
    String accountStatus
) {
}
