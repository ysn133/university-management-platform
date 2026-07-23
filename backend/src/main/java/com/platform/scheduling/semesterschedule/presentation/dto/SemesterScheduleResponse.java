package com.platform.scheduling.semesterschedule.presentation.dto;

import com.platform.scheduling.semesterschedule.domain.SchedulePublicationStatus;
import java.time.Instant;
import java.util.UUID;

public record SemesterScheduleResponse(
    UUID id,
    UUID establishmentId,
    UUID academicYearId,
    UUID semesterId,
    SchedulePublicationStatus publicationStatus,
    Instant publishedAt,
    Instant createdAt,
    Instant updatedAt
) {
}
