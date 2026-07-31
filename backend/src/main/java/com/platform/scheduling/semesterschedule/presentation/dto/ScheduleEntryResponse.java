package com.platform.scheduling.semesterschedule.presentation.dto;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleEntryResponse(
    UUID id,
    UUID semesterScheduleId,
    UUID teachingAssignmentId,
    UUID professorId,
    UUID subjectModuleId,
    UUID teachingGroupId,
    String teachingGroupName,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    String location,
    Instant createdAt,
    Instant updatedAt
) {
}
