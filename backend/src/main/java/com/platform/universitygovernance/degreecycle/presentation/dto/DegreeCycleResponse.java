package com.platform.universitygovernance.degreecycle.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record DegreeCycleResponse(
    UUID id,
    UUID establishmentId,
    String name,
    Instant createdAt,
    Instant updatedAt
) {
}
