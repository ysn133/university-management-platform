package com.platform.scheduling.teachinggroup.presentation.dto;

import java.util.List;
import java.util.UUID;

public record TeachingGroupRosterResponse(
    UUID semesterId,
    List<TeachingGroupResponse> groups
) {
}
