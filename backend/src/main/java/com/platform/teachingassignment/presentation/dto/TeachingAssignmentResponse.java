package com.platform.teachingassignment.presentation.dto;

import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import java.time.Instant;
import java.util.UUID;

public record TeachingAssignmentResponse(
    UUID id,
    UUID establishmentId,
    UUID professorId,
    UUID subjectModuleId,
    UUID classGroupId,
    UUID academicYearId,
    UUID semesterId,
    TeachingAssignmentStatus status,
    Instant createdAt,
    Instant updatedAt
) {
}
