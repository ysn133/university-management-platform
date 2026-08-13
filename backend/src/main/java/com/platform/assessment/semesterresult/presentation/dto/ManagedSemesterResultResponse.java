package com.platform.assessment.semesterresult.presentation.dto;

import com.platform.assessment.semesterresult.domain.SemesterResultStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ManagedSemesterResultResponse(
    UUID id,
    UUID semesterRegistrationId,
    UUID studentId,
    String firstName,
    String lastName,
    String apogeeCode,
    BigDecimal semesterAverage,
    SemesterResultStatus resultStatus,
    long validatedModuleCount,
    long compensatedModuleCount,
    long nonValidatedModuleCount,
    Instant evaluatedAt,
    boolean secondInscriptionOnly,
    UUID originalAcademicYearId,
    UUID originalAcademicLevelId,
    UUID originalSemesterId,
    UUID originalClassGroupId,
    String originalAcademicYearLabel,
    String originalAcademicLevelName,
    String originalSemesterName
) {
}
