package com.platform.scheduling.examschedule.presentation.dto;

import java.util.UUID;
import java.time.LocalDate;

import com.platform.scheduling.examschedule.domain.ExamSessionType;
import jakarta.validation.constraints.NotNull;

public record CreateExamSchedule(
    @NotNull UUID academicYearId,
    @NotNull UUID semesterId,
    @NotNull ExamSessionType sessionType,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate
) {
    public CreateExamSchedule(UUID academicYearId, UUID semesterId, ExamSessionType sessionType) {
        this(academicYearId, semesterId, sessionType, LocalDate.of(2000, 7, 1), LocalDate.of(2100, 12, 31));
    }
}
