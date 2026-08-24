package com.platform.academicregistration.registration.presentation.dto;

import com.platform.academicregistration.moduleregistration.domain.ModuleRegistrationStatus;
import java.time.LocalDate;
import java.util.UUID;

public record StudentModuleRegistrationResponse(
    UUID moduleRegistrationId,
    UUID subjectModuleId,
    String subjectModuleCode,
    String subjectModuleTitle,
    UUID academicRegistrationId,
    UUID semesterRegistrationId,
    UUID academicYearId,
    String academicYearLabel,
    String academicYearStatus,
    UUID programPathId,
    String programPathName,
    UUID programFiliereId,
    String programFiliereCode,
    String programFiliereName,
    UUID academicLevelId,
    String academicLevelName,
    UUID semesterId,
    String semesterName,
    LocalDate semesterStartDate,
    LocalDate semesterEndDate,
    UUID originAcademicLevelId,
    String originAcademicLevelName,
    int inscriptionNumber,
    ModuleRegistrationStatus status
) {
}
