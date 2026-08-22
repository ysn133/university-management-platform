package com.platform.scheduling.examcandidate.presentation.dto;

import com.platform.scheduling.examschedule.domain.ExamSessionType;
import com.platform.universitygovernance.academicyear.domain.AcademicYearStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record StudentExamInvitationResponse(
    UUID id,
    UUID moduleExamId,
    UUID subjectModuleId,
    String subjectModuleCode,
    String subjectModuleTitle,
    UUID academicYearId,
    String academicYearLabel,
    AcademicYearStatus academicYearStatus,
    UUID semesterId,
    String semesterName,
    LocalDate semesterStartDate,
    LocalDate semesterEndDate,
    UUID academicLevelId,
    String academicLevelName,
    UUID programFiliereId,
    String programFiliereCode,
    String programFiliereName,
    ExamSessionType sessionType,
    LocalDate examDate,
    LocalTime startTime,
    LocalTime endTime,
    String examGroupLabel,
    String roomCode
) {
}
