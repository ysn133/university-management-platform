package com.platform.teachingassignment.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateTeachingAssignmentRequest(
    @NotNull UUID professorId,
    @NotNull UUID teachingRequirementId
) {
}
