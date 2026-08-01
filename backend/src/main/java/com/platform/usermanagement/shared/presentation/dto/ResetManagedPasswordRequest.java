package com.platform.usermanagement.shared.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetManagedPasswordRequest(
    @NotBlank @Size(min = 8, max = 255) String newPassword
) {
}
