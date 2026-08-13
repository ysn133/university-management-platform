package com.platform.scheduling.moduleexam.presentation.dto;

import com.platform.scheduling.examschedule.domain.ExamSessionType;
import com.platform.universitygovernance.academicyear.domain.AcademicYearStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record ProfessorExamResponse(
    UUID id,
    UUID subjectModuleId,
    String subjectModuleCode,
    String subjectModuleTitle,
    UUID classGroupId,
    String classGroupName,
    UUID academicYearId,
    String academicYearLabel,
    AcademicYearStatus academicYearStatus,
    UUID semesterId,
    String semesterName,
    LocalDate semesterStartDate,
    LocalDate semesterEndDate,
    UUID academicLevelId,
    String academicLevelName,
    String programFiliereCode,
    String programFiliereName,
    ExamSessionType sessionType,
    LocalDate examDate,
    LocalTime startTime,
    LocalTime endTime,
    List<String> rooms
) {
}
