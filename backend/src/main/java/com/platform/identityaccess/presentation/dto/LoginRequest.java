package com.platform.identityaccess.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank
    @Email
    String universityEmail,

    @NotBlank
    @Size(min = 8, max = 255)
    String password
) {
}