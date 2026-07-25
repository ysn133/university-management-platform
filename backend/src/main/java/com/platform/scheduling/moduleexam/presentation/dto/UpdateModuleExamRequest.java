package com.platform.scheduling.moduleexam.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record UpdateModuleExamRequest(
    @NotNull UUID subjectModuleId,
    @NotNull UUID classGroupId,
    UUID teachingAssignmentId,
    @NotNull LocalDate examDate,
    @NotNull LocalTime startTime,
    LocalTime endTime,
    @Size(max = 255) String location
) {
}
