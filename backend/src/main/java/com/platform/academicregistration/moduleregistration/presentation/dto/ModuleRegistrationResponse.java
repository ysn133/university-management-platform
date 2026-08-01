package com.platform.academicregistration.moduleregistration.presentation.dto;

import com.platform.academicregistration.moduleregistration.domain.ModuleRegistrationStatus;
import java.util.UUID;

public record ModuleRegistrationResponse(
    UUID id,
    UUID semesterRegistrationId,
    UUID subjectModuleId,
    String subjectModuleCode,
    String subjectModuleTitle,
    UUID originAcademicLevelId,
    int inscriptionNumber,
    ModuleRegistrationStatus status
) {
}
