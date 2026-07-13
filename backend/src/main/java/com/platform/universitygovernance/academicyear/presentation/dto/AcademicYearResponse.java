package com.platform.universitygovernance.academicyear.presentation.dto;

import com.platform.universitygovernance.academicyear.domain.AcademicYearStatus;
import java.time.Instant;
import java.util.UUID;

public record AcademicYearResponse(
    UUID id,
    UUID establishmentId,
    String label,
    int startYear,
    int endYear,
    AcademicYearStatus status,
    Instant createdAt,
    Instant updatedAt
) {
}
