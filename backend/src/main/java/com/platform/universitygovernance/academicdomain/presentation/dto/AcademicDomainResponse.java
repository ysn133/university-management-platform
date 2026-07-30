package com.platform.universitygovernance.academicdomain.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record AcademicDomainResponse(
    UUID id,
    UUID establishmentId,
    String code,
    String name,
    Instant createdAt,
    Instant updatedAt
) {
}
