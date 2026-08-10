package com.platform.teachingassignment.presentation.dto;

import java.util.UUID;

public record ProfessorTeachingWorkloadResponse(
    UUID professorId,
    String employeeNumber,
    int assignedWeeklyMinutes,
    int maximumWeeklyTeachingMinutes
) {
}
