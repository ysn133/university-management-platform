package com.platform.academicregistration.registration.presentation.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record StudentAcademicContextResponse(
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
    String registrationStatus,
    UUID classGroupId,
    String classGroupName,
    List<String> tdGroups,
    List<String> tpGroups
) {
}
