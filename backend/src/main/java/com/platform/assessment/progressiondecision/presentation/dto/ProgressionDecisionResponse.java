package com.platform.assessment.progressiondecision.presentation.dto;

import com.platform.assessment.progressiondecision.domain.ProgressionDecisionStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProgressionDecisionResponse(
    UUID id,
    UUID academicRegistrationId,
    UUID academicRuleProfileId,
    ProgressionDecisionStatus decisionStatus,
    BigDecimal annualAverage,
    int outstandingModuleCount,
    Instant decidedAt
) {
}
