package com.platform.academicregistration.classassignment.presentation.dto;

import java.util.List;
import java.util.UUID;

public record ClassGroupRosterGroupResponse(
    UUID classGroupId,
    String name,
    List<UUID> academicRegistrationIds
) {
}
