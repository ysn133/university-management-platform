package com.platform.universitygovernance.degreecycle.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDegreeCycleRequest(
    @NotBlank @Size(max = 255) String name
) {
}
