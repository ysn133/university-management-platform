package com.platform.assessment.graduationdecision.presentation.dto;

import com.platform.assessment.graduationdecision.domain.GraduationDecisionStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GraduationDecisionResponse(
    UUID id,
    UUID studentId,
    String firstName,
    String lastName,
    String apogeeCode,
    String nationalStudentCode,
    String cin,
    String programName,
    String programPathName,
    String degreeCycleName,
    String terminalAcademicLevelName,
    String academicYearLabel,
    GraduationDecisionStatus decisionStatus,
    BigDecimal graduationAverage,
    Instant decidedAt
) {
}
