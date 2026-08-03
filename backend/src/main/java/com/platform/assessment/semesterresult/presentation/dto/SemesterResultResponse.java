package com.platform.assessment.semesterresult.presentation.dto;

import com.platform.assessment.semesterresult.domain.SemesterResultStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SemesterResultResponse(
    UUID id,
    UUID semesterRegistrationId,
    UUID academicRuleProfileId,
    BigDecimal semesterAverage,
    SemesterResultStatus resultStatus,
    Instant evaluatedAt
) {
}
