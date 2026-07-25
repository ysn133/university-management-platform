package com.platform.scheduling.moduleexam.presentation.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ModuleExamResponse(
    UUID id,
    UUID examScheduleId,
    UUID subjectModuleId,
    UUID classGroupId,
    UUID teachingAssignmentId,
    LocalDate examDate,
    LocalTime startTime,
    LocalTime endTime,
    String location,
    Instant createdAt,
    Instant updatedAt
) {
}
