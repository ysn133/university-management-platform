package com.platform.universitygovernance.semester.presentation.dto;

import java.time.Instant;
import java.util.UUID;
import com.platform.universitygovernance.semester.domain.SemesterTermType;

public record SemesterResponse(
    UUID id,
    UUID academicLevelId,
    UUID academicYearId,
    UUID establishmentId,
    String name,
    int semesterOrder,
    SemesterTermType termType,
    Instant createdAt,
    Instant updatedAt
) {
}
