package com.platform.scheduling.examgroup.presentation.dto;

import java.util.UUID;

public record ExamGroupMemberResponse(
    UUID studentId,
    String apogeeCode,
    String nationalStudentCode,
    String cin,
    String lastName,
    String firstName
) {}
