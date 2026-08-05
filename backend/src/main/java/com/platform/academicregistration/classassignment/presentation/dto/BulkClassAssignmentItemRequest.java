package com.platform.academicregistration.classassignment.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record BulkClassAssignmentItemRequest(
    @NotNull UUID academicRegistrationId,
    @NotNull UUID classGroupId
) {
}
