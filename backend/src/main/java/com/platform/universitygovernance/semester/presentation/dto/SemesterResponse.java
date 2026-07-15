package com.platform.universitygovernance.semester.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record SemesterResponse(
    UUID id,
    UUID academicLevelId,
    UUID academicYearId,
    UUID establishmentId,
    String name,
    int semesterOrder,
    Instant createdAt,
    Instant updatedAt
) {
}
