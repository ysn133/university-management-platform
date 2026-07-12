package com.platform.universitygovernance.programpath.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProgramPathRequest(
    @NotBlank @Size(max = 255) String name
) {
}
