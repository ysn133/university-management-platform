package com.platform.scheduling.examgroup.presentation.dto;

import java.util.UUID;
import java.util.List;

public record ExamGroupResponse(
    UUID id,
    String label,
    int groupOrder,
    long studentCount,
    List<ExamGroupMemberResponse> members
) {}
