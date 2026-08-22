package com.platform.attendance.absencerecord.presentation.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AbsenceRecordResponse(
    UUID id,
    UUID moduleRegistrationId,
    UUID studentId,
    UUID subjectModuleId,
    String subjectModuleCode,
    String subjectModuleTitle,
    UUID academicYearId,
    String academicYearLabel,
    UUID semesterId,
    String semesterName,
    UUID teachingAssignmentId,
    UUID recordedByProfessorId,
    LocalDate absenceDate,
    boolean justified,
    String justificationNote,
    Instant createdAt,
    Instant updatedAt
) {
}
