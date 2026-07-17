package com.platform.universitygovernance.subjectmodules.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSubjectModuleRequest(
    @NotBlank @Size(max = 255) String code,
    @NotBlank @Size(max = 255) String title
) {
}
