package com.platform.assessment.graderecord.presentation.dto;

import com.platform.assessment.graderecord.domain.GradeWorkflowStatus;
import com.platform.assessment.graderecord.domain.ZeroGradeReason;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GradeItemResponse(
    UUID gradeRecordId,
    UUID moduleRegistrationId,
    UUID studentId,
    String apogeeCode,
    String universityEmail,
    String firstName,
    String lastName,
    int inscriptionNumber,
    BigDecimal gradeValue,
    ZeroGradeReason zeroGradeReason,
    GradeWorkflowStatus workflowStatus,
    Instant publishedAt
) {
}
