package com.platform.scheduling.examgroup.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record GenerateExamGroupsRequest(
    @Min(1) @Max(20) int splitCount
) {}
