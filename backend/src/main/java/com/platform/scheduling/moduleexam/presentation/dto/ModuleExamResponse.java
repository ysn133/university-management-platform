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
    LocalDate examDate,
    LocalTime startTime,
    LocalTime endTime,
    String location,
    Instant candidateListGeneratedAt,
    Instant createdAt,
    Instant updatedAt
) {
}
