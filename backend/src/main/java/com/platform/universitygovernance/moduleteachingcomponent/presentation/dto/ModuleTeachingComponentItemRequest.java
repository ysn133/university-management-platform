package com.platform.universitygovernance.moduleteachingcomponent.presentation.dto;

import com.platform.scheduling.domain.RoomType;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Set;
import java.util.UUID;

public record ModuleTeachingComponentItemRequest(
    @NotNull TeachingComponentType componentType,
    @Positive int sessionsPerWeek,
    @Positive int sessionDurationMinutes,
    @NotNull TeachingAudienceMode audienceMode,
    @Positive Integer maximumGroupSize,
    @NotNull RoomType requiredRoomType,
    @NotNull Set<UUID> requiredDomainIds
) {
}
