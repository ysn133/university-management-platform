package com.platform.assessment.progressiondecision.presentation.dto;

import com.platform.assessment.moduleresult.domain.ModuleResultStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record AcademicYearModuleResultResponse(
    UUID subjectModuleId,
    String subjectModuleCode,
    String subjectModuleTitle,
    BigDecimal finalGrade,
    ModuleResultStatus resultStatus,
    int inscriptionNumber
) {
}
