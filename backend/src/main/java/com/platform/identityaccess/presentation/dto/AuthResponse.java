package com.platform.identityaccess.presentation.dto;

import java.util.UUID;

public record AuthResponse(
    UUID userAccountId,
    String role,
    UUID roleEntityId,
    UUID establishmentId,
    String universityEmail,
    String firstName,
    String lastName,
    String accountStatus,
    String accessToken,
    String refreshToken
) {
}
