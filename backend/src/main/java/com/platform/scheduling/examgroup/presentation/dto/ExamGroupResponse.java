package com.platform.scheduling.examgroup.presentation.dto;

import java.util.UUID;

public record ExamGroupResponse(
    UUID id,
    String label,
    int groupOrder,
    long studentCount
) {}
