package com.platform.moduleclassresponsibility.presentation.dto;

import com.platform.moduleclassresponsibility.domain.ModuleClassResponsibilityStatus;
import java.time.Instant;
import java.util.UUID;

public record ModuleClassResponsibilityResponse(
    UUID id,
    UUID establishmentId,
    UUID professorId,
    UUID subjectModuleId,
    UUID classGroupId,
    UUID academicYearId,
    UUID semesterId,
    ModuleClassResponsibilityStatus status,
    Instant createdAt,
    Instant updatedAt
) {
}
