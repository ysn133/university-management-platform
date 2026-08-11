package com.platform.universitygovernance.semester.presentation.dto;

import java.time.Instant;
import java.util.UUID;
import com.platform.universitygovernance.semester.domain.SemesterTermType;
import com.platform.universitygovernance.semester.domain.SemesterLifecycleStatus;
import java.time.LocalDate;

public record SemesterResponse(
    UUID id,
    UUID academicLevelId,
    UUID academicYearId,
    UUID establishmentId,
    String name,
    int semesterOrder,
    SemesterTermType termType,
    LocalDate startDate,
    LocalDate endDate,
    SemesterLifecycleStatus lifecycleStatus,
    Instant createdAt,
    Instant updatedAt
) {
}
