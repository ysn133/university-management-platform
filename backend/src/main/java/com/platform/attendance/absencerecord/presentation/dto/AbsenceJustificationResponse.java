package com.platform.attendance.absencerecord.presentation.dto;

import com.platform.attendance.absencerecord.domain.AbsenceJustificationStatus;
import java.time.*;
import java.util.UUID;

public record AbsenceJustificationResponse(UUID id, UUID absenceId, UUID teachingAssignmentId, UUID studentId,
    String studentApogeeCode, String studentFirstName, String studentLastName, UUID subjectModuleId,
    String subjectModuleCode, String subjectModuleTitle, LocalDate absenceDate, String reason,
    AbsenceJustificationStatus status, UUID documentId, String documentFileName, String documentContentType,
    String decisionNote, Instant submittedAt, Instant reviewedAt) {
}
