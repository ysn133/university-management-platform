package com.platform.assessment.graderecord.presentation.dto;

import com.platform.assessment.graderecord.domain.ZeroGradeReason;
import com.platform.scheduling.examschedule.domain.ExamSessionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StudentGradeResponse(
    UUID gradeRecordId,
    UUID moduleRegistrationId,
    UUID moduleExamId,
    UUID subjectModuleId,
    UUID academicYearId,
    UUID semesterId,
    ExamSessionType sessionType,
    int inscriptionNumber,
    BigDecimal gradeValue,
    ZeroGradeReason zeroGradeReason,
    Instant publishedAt
) {
}
