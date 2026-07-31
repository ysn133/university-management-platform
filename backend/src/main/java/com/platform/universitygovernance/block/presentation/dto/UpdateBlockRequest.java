package com.platform.universitygovernance.block.presentation.dto;

import com.platform.universitygovernance.block.domain.BlockStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateBlockRequest(
    @NotBlank @Size(max = 50) String code,
    @NotBlank @Size(max = 255) String name,
    @NotNull BlockStatus status
) {
}
