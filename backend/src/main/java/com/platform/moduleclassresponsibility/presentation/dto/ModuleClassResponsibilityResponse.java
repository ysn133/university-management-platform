package com.platform.moduleclassresponsibility.presentation.dto;

import com.platform.moduleclassresponsibility.domain.ModuleClassResponsibilityStatus;
import java.time.Instant;
import java.util.UUID;

public record ModuleClassResponsibilityResponse(
    UUID id,
    UUID establishmentId,
    UUID professorId,
    UUID subjectModuleId,
    String subjectModuleCode,
    String subjectModuleTitle,
    UUID classGroupId,
    String classGroupName,
    UUID academicYearId,
    String academicYearLabel,
    UUID semesterId,
    String semesterName,
    ModuleClassResponsibilityStatus status,
    Instant createdAt,
    Instant updatedAt
) {
}
