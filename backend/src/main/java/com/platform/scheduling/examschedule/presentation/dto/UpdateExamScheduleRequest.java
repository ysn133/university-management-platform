package com.platform.scheduling.examschedule.presentation.dto;

import com.platform.scheduling.examschedule.domain.ExamSessionType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import java.time.LocalDate;

public record UpdateExamScheduleRequest(
    @NotNull UUID academicYearId,
    @NotNull UUID semesterId,
    @NotNull ExamSessionType sessionType,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate
) {
    public UpdateExamScheduleRequest(UUID academicYearId, UUID semesterId, ExamSessionType sessionType) {
        this(academicYearId, semesterId, sessionType, LocalDate.of(2000, 7, 1), LocalDate.of(2100, 12, 31));
    }
}
