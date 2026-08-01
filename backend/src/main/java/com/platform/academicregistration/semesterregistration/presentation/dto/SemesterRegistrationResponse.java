package com.platform.academicregistration.semesterregistration.presentation.dto;

import java.util.UUID;

public record SemesterRegistrationResponse(
    UUID id,
    UUID academicRegistrationId,
    UUID semesterId,
    String semesterName,
    int semesterOrder
) {
}
