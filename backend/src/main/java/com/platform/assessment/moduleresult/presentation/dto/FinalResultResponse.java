package com.platform.assessment.moduleresult.presentation.dto;

import com.platform.assessment.moduleresult.domain.ModuleResultStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record FinalResultResponse(
    UUID moduleRegistrationId,
    UUID studentId,
    String firstName,
    String lastName,
    String apogeeCode,
    UUID subjectModuleId,
    String subjectModuleCode,
    String subjectModuleTitle,
    int inscriptionNumber,
    BigDecimal finalGrade,
    ModuleResultStatus resultStatus
) {}
