package com.platform.scheduling.examschedule.presentation.dto;

import java.util.UUID;

import com.platform.scheduling.examschedule.domain.ExamSessionType;
import jakarta.validation.constraints.NotNull;

public record CreateExamSchedule(
    @NotNull UUID academicYearId,
    @NotNull UUID semesterId,
    @NotNull ExamSessionType sessionType
) {
}
