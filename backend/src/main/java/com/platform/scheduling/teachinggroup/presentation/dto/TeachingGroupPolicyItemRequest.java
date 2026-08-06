package com.platform.scheduling.teachinggroup.presentation.dto;

import com.platform.scheduling.teachinggroup.domain.TeachingGroupType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TeachingGroupPolicyItemRequest(
    @NotNull TeachingGroupType groupType,
    @Positive int maximumGroupSize
) {
}
