package com.platform.universitygovernance.moduleteachingcomponent.presentation.dto;

import com.platform.scheduling.domain.RoomType;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ModuleTeachingComponentResponse(
    UUID id,
    UUID subjectModuleId,
    TeachingComponentType componentType,
    int sessionsPerWeek,
    int sessionDurationMinutes,
    TeachingAudienceMode audienceMode,
    Integer maximumGroupSize,
    RoomType requiredRoomType,
    List<UUID> requiredDomainIds,
    Instant createdAt,
    Instant updatedAt
) {
}
