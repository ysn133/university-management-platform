package com.platform.identityaccess.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record ChangePasswordRequest(
    @NotBlank
    @Size(min = 8, max = 255)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String currentPassword,

    @NotBlank
    @Size(min = 8, max = 255)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String newPassword
) {
}
