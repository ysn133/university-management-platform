package com.platform.universitygovernance.establishment.presentation.dto;

import com.platform.universitygovernance.establishment.domain.EstablishmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateEstablishmentRequest(
    @NotBlank String name,
    @NotNull EstablishmentType type
) {
}
