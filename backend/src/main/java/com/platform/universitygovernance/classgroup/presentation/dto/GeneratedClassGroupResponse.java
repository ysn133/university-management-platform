package com.platform.universitygovernance.classgroup.presentation.dto;

import java.util.UUID;

public record GeneratedClassGroupResponse(
    UUID classGroupId,
    String name,
    int studentCount
) {
}
