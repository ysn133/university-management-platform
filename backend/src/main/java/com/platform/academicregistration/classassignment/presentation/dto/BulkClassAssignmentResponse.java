package com.platform.academicregistration.classassignment.presentation.dto;

import java.util.UUID;

public record BulkClassAssignmentResponse(
    UUID academicLevelId,
    UUID academicYearId,
    int studentsProcessed,
    int semesterAssignmentsCreated
) {
}
