package com.platform.universitygovernance.department.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDepartmentRequest(
    @NotBlank @Size(max = 255) String name
) {
}
