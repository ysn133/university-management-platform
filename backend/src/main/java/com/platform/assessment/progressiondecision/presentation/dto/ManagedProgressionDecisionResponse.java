package com.platform.assessment.progressiondecision.presentation.dto;

import com.platform.assessment.progressiondecision.domain.ProgressionDecisionStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ManagedProgressionDecisionResponse(
    UUID id,
    UUID academicRegistrationId,
    UUID studentId,
    String firstName,
    String lastName,
    String apogeeCode,
    String nationalStudentCode,
    String cin,
    String programName,
    String programPathName,
    String academicLevelName,
    String academicYearLabel,
    List<AcademicYearSemesterResultResponse> semesterResults,
    ProgressionDecisionStatus decisionStatus,
    BigDecimal annualAverage,
    int outstandingModuleCount,
    Instant decidedAt
) {
}
