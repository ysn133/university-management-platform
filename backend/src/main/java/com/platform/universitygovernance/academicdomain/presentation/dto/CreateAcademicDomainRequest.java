package com.platform.universitygovernance.academicdomain.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAcademicDomainRequest(
    @NotBlank @Size(max = 50) String code,
    @NotBlank @Size(max = 255) String name
) {
}
