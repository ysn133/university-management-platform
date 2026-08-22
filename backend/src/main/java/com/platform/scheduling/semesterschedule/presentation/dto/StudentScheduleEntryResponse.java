package com.platform.scheduling.semesterschedule.presentation.dto;

import com.platform.universitygovernance.academicyear.domain.AcademicYearStatus;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import com.platform.universitygovernance.semester.domain.SemesterTermType;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record StudentScheduleEntryResponse(
    UUID id,
    UUID academicYearId,
    String academicYearLabel,
    AcademicYearStatus academicYearStatus,
    UUID semesterId,
    String semesterName,
    SemesterTermType semesterTermType,
    LocalDate semesterStartDate,
    LocalDate semesterEndDate,
    UUID academicLevelId,
    String academicLevelName,
    UUID programFiliereId,
    String programFiliereCode,
    String programFiliereName,
    UUID subjectModuleId,
    String subjectModuleCode,
    String subjectModuleTitle,
    TeachingComponentType componentType,
    TeachingAudienceMode audienceType,
    String teachingGroupName,
    String professorName,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    String roomCode,
    String roomName,
    String blockCode,
    String blockName
) {
}
