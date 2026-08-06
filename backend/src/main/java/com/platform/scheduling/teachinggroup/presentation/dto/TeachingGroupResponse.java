package com.platform.scheduling.teachinggroup.presentation.dto;

import com.platform.scheduling.teachinggroup.domain.TeachingGroupType;
import java.util.List;
import java.util.UUID;

public record TeachingGroupResponse(
    UUID id,
    UUID semesterId,
    UUID sourceClassGroupId,
    String sourceClassGroupName,
    String name,
    TeachingGroupType groupType,
    List<TeachingGroupMemberResponse> members
) {
}
