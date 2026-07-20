package com.platform.academicregistration.registration.presentation.dto;

import com.platform.academicregistration.registration.domain.AcademicRegistrationStatus;
import java.time.Instant;
import java.util.UUID;

public record AcademicRegistrationResponse(
    UUID id,
    UUID studentId,
    UUID establishmentId,
    UUID programFiliereId,
    UUID academicLevelId,
    UUID academicYearId,
    AcademicRegistrationStatus status,
    Instant createdAt,
    Instant updatedAt
) {
}
