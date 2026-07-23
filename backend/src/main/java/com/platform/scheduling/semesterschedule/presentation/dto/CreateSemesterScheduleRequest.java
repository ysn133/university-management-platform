package com.platform.scheduling.semesterschedule.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateSemesterScheduleRequest(
    @NotNull UUID academicYearId,
    @NotNull UUID semesterId
) {
}
