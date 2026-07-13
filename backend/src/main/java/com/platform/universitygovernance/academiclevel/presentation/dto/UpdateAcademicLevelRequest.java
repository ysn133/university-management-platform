package com.platform.universitygovernance.academiclevel.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateAcademicLevelRequest(
    @NotBlank @Size(max = 100) String name,
    @Positive @Max(32767) int levelOrder
) {
}
