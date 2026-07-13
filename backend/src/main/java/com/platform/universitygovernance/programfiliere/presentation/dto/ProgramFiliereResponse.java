package com.platform.universitygovernance.programfiliere.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record ProgramFiliereResponse(
    UUID id,
    UUID departmentId,
    UUID establishmentId,
    UUID degreeCycleId,
    UUID programPathId,
    String code,
    String name,
    Instant createdAt,
    Instant updatedAt
) {
}
