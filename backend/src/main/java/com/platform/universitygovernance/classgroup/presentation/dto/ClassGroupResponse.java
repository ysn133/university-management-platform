package com.platform.universitygovernance.classgroup.presentation.dto;

import com.platform.universitygovernance.classgroup.domain.ClassGroupStatus;
import java.time.Instant;
import java.util.UUID;

public record ClassGroupResponse(
    UUID id,
    UUID academicLevelId,
    UUID academicYearId,
    UUID programFiliereId,
    UUID establishmentId,
    String name,
    ClassGroupStatus status,
    Instant createdAt,
    Instant updatedAt
) {
}
