package com.platform.universitygovernance.block.presentation.dto;

import com.platform.universitygovernance.block.domain.BlockStatus;
import java.time.Instant;
import java.util.UUID;

public record BlockResponse(
    UUID id,
    UUID establishmentId,
    String code,
    String name,
    BlockStatus status,
    Instant createdAt,
    Instant updatedAt
) {
}
