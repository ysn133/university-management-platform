package com.platform.scheduling.teachinggroup.presentation.dto;

import com.platform.scheduling.teachinggroup.domain.TeachingGroupType;
import java.time.Instant;
import java.util.UUID;

public record TeachingGroupPolicyResponse(
    UUID id,
    UUID academicLevelId,
    UUID academicYearId,
    TeachingGroupType groupType,
    int minimumGroupSize,
    int maximumGroupSize,
    Instant createdAt,
    Instant updatedAt
) {
}
