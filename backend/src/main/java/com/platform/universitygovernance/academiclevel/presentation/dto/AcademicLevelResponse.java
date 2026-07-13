package com.platform.universitygovernance.academiclevel.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record AcademicLevelResponse(
    UUID id,
    UUID programFiliereId,
    UUID establishmentId,
    String name,
    int levelOrder,
    Instant createdAt,
    Instant updatedAt
) {
}
