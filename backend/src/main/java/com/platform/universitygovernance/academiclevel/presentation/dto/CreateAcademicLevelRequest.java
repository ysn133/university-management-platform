package com.platform.universitygovernance.academiclevel.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateAcademicLevelRequest(
    @NotBlank @Size(max = 100) String name,
    @Positive @Max(32767) int levelOrder,
    boolean terminalLevel,
    @NotNull UUID initialAcademicYearId,
    @NotNull UUID academicRuleProfileId
) {
}
