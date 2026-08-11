package com.platform.scheduling.semesterschedule.presentation.dto;

import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode;
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
    UUID sourceClassGroupId,
    String sourceClassGroupName,
    TeachingAudienceMode audienceType,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    UUID roomId,
    String roomCode,
    String roomName,
    UUID blockId,
    Instant createdAt,
    Instant updatedAt
) {
}
