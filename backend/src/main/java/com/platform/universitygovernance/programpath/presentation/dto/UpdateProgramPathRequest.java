package com.platform.universitygovernance.programpath.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProgramPathRequest(
    @NotBlank @Size(max = 255) String name
) {
}
