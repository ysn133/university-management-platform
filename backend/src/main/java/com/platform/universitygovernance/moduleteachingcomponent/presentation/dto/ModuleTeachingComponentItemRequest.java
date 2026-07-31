package com.platform.universitygovernance.moduleteachingcomponent.presentation.dto;

import com.platform.scheduling.domain.RoomType;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ModuleTeachingComponentItemRequest(
    @NotNull TeachingComponentType componentType,
    @Positive int sessionsPerWeek,
    @Positive int sessionDurationMinutes,
    @NotNull TeachingAudienceMode audienceMode,
    @Positive Integer maximumGroupSize,
    @NotNull RoomType requiredRoomType
) {
}
