package com.platform.scheduling.examgroup.presentation.dto;

import java.util.List;
import java.util.UUID;

public record ExamGroupPlanResponse(
    UUID examScheduleId,
    UUID classGroupId,
    int totalStudentCount,
    int splitCount,
    List<ExamGroupResponse> groups
) {}
