package com.platform.academicregistration.classassignment.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record StudentClassAssignmentResponse(
    UUID id,
    UUID academicRegistrationId,
    UUID semesterRegistrationId,
    UUID semesterId,
    UUID classGroupId,
    Instant createdAt,
    Instant updatedAt
) {
}
