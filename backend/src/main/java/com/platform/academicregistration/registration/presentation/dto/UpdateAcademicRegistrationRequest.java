package com.platform.academicregistration.registration.presentation.dto;

import com.platform.academicregistration.registration.domain.AcademicRegistrationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAcademicRegistrationRequest(
    @NotNull AcademicRegistrationStatus status
) {
}
