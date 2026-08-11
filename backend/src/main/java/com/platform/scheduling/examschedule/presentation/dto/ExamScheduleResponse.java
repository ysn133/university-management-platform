package com.platform.scheduling.examschedule.presentation.dto;

import java.time.Instant;
import java.util.UUID;
import java.time.LocalDate;

import com.platform.scheduling.examschedule.domain.ExamSessionType;
import com.platform.scheduling.examschedule.domain.PublicationStatus;

public record ExamScheduleResponse(
    UUID id,
    UUID establishmentId,
    UUID academicYearId,
    UUID semesterId,
    ExamSessionType sessionType,
    PublicationStatus publicationStatus,
    LocalDate startDate,
    LocalDate endDate,
    Instant createdAt,
    Instant updatedAt
) {
}
