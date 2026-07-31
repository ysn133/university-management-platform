package com.platform.moduleclassresponsibility.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateModuleClassResponsibilityRequest(
    @NotNull UUID professorId,
    @NotNull UUID subjectModuleId,
    @NotNull UUID classGroupId,
    @NotNull UUID academicYearId,
    @NotNull UUID semesterId
) {
}
