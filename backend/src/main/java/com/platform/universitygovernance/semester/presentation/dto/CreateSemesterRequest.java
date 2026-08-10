package com.platform.universitygovernance.semester.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import com.platform.universitygovernance.semester.domain.SemesterTermType;

public record CreateSemesterRequest(
    @NotBlank @Size(max = 100) String name,
    @Positive @Max(32767) int semesterOrder,
    @NotNull SemesterTermType termType
) {
}
