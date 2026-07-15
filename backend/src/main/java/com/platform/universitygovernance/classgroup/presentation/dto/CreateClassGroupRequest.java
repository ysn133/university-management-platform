package com.platform.universitygovernance.classgroup.presentation.dto;

import com.platform.universitygovernance.classgroup.domain.ClassGroupStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateClassGroupRequest(
    @NotBlank @Size(max = 100) String name,
    @NotNull ClassGroupStatus status
) {
}
