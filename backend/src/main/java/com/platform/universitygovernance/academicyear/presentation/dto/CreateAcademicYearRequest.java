package com.platform.universitygovernance.academicyear.presentation.dto;

import com.platform.universitygovernance.academicyear.domain.AcademicYearStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateAcademicYearRequest(
    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{4}", message = "label must use the format YYYY-YYYY")
    String label,
    @NotNull AcademicYearStatus status
) {
}
