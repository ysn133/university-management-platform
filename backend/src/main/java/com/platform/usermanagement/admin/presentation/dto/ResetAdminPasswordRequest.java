package com.platform.usermanagement.admin.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetAdminPasswordRequest(
    @NotBlank
    @Size(min = 8, max = 255)
    String newPassword
) {
}
