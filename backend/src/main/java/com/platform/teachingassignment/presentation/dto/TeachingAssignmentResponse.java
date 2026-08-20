package com.platform.teachingassignment.presentation.dto;

import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import com.platform.teachingassignment.domain.TeachingAssignmentSource;
import com.platform.universitygovernance.academicyear.domain.AcademicYearStatus;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import com.platform.universitygovernance.semester.domain.SemesterLifecycleStatus;
import com.platform.universitygovernance.semester.domain.SemesterTermType;
import java.time.Instant;
import java.util.UUID;

public record TeachingAssignmentResponse(
    UUID id,
    UUID establishmentId,
    UUID professorId,
    UUID teachingRequirementId,
    UUID subjectModuleId,
    String subjectModuleCode,
    String subjectModuleTitle,
    TeachingComponentType componentType,
    int sessionsPerWeek,
    int sessionDurationMinutes,
    UUID teachingGroupId,
    String teachingGroupName,
    UUID semesterId,
    String semesterName,
    SemesterTermType semesterTermType,
    SemesterLifecycleStatus semesterLifecycleStatus,
    UUID academicYearId,
    String academicYearLabel,
    AcademicYearStatus academicYearStatus,
    UUID academicLevelId,
    String academicLevelName,
    UUID programFiliereId,
    String programFiliereCode,
    String programFiliereName,
    TeachingAssignmentStatus status,
    TeachingAssignmentSource assignmentSource,
    Instant createdAt,
    Instant updatedAt
) {
}
