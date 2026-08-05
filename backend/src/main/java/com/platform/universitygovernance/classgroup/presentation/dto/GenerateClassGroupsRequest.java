package com.platform.universitygovernance.classgroup.presentation.dto;

import jakarta.validation.constraints.Min;

public record GenerateClassGroupsRequest(
    @Min(1) int minimumGroupSize,
    @Min(1) int maximumGroupSize
) {
}
