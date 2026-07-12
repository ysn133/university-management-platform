package com.platform.universitygovernance.programpath.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record ProgramPathResponse(
    UUID id,
    UUID establishmentId,
    String name,
    Instant createdAt,
    Instant updatedAt
) {
}
