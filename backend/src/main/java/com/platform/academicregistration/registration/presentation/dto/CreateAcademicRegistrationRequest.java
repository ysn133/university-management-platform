package com.platform.academicregistration.registration.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateAcademicRegistrationRequest(
    @NotNull UUID studentId,
    @NotNull UUID programFiliereId,
    @NotNull UUID academicLevelId,
    @NotNull UUID academicYearId
) {
}
