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
    UUID roomId,
    String roomCode,
    String roomName,
    Instant candidateListGeneratedAt,
    Instant createdAt,
    Instant updatedAt
) {
    public String location() { return roomCode; }
}
