package com.platform.scheduling.examschedule.presentation.dto;

import com.platform.scheduling.examschedule.domain.ExamSessionType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UpdateExamScheduleRequest(
    @NotNull UUID academicYearId,
    @NotNull UUID semesterId,
    @NotNull ExamSessionType sessionType
) {
}
