package com.platform.assessment.graderecord.presentation.dto;

import com.platform.assessment.graderecord.domain.ZeroGradeReason;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record GradeItemRequest(
    @NotNull UUID moduleRegistrationId,
    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("20.00")
    @Digits(integer = 2, fraction = 2)
    BigDecimal gradeValue,
    ZeroGradeReason zeroGradeReason
) {
}
