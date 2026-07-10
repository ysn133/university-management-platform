package com.platform.universitygovernance.department.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDepartmentRequest(
    @NotBlank @Size(max = 255) String name
) {
}
