package com.platform.assessment.progressiondecision.presentation.dto;

import com.platform.assessment.semesterresult.domain.SemesterResultStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AcademicYearSemesterResultResponse(
    UUID semesterId,
    String semesterName,
    int semesterOrder,
    BigDecimal semesterAverage,
    SemesterResultStatus resultStatus,
    List<AcademicYearModuleResultResponse> moduleResults
) {
}
