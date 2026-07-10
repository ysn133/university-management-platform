package com.platform.universitygovernance.establishment.presentation.dto;

import java.util.UUID;

import com.platform.universitygovernance.establishment.domain.EstablishmentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEstablishmentRequest(
    @NotNull UUID universityId,
    @NotBlank String name,
    @NotNull EstablishmentType type
) {
    
}
