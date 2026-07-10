package com.platform.usermanagement.superadmin.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank
    @Size(min = 8, max = 255)
    String newPassword
) {
}
