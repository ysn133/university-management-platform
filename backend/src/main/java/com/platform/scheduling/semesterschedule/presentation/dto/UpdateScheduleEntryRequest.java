package com.platform.scheduling.semesterschedule.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record UpdateScheduleEntryRequest(
    @NotNull UUID teachingAssignmentId,
    @NotNull DayOfWeek dayOfWeek,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    @NotNull UUID roomId
) {
}
