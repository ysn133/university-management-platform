package com.platform.teachingassignment.presentation.dto;

import java.util.UUID;

public record TeachingAssignmentStudentResponse(
    UUID studentId,
    String apogeeCode,
    String nationalStudentCode,
    String universityEmail,
    String firstName,
    String lastName
) {
}
