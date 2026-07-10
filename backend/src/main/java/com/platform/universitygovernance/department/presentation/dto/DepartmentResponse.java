package com.platform.universitygovernance.department.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record DepartmentResponse(
    UUID id,
    UUID establishmentId,
    String name,
    Instant createdAt,
    Instant updatedAt
) {
}
