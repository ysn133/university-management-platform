package com.platform.universitygovernance.programfiliere.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateProgramFiliereRequest(
    @NotBlank @Size(max = 100) String code,
    @NotBlank @Size(max = 255) String name,
    @NotNull UUID degreeCycleId,
    @NotNull UUID programPathId
) {
}
