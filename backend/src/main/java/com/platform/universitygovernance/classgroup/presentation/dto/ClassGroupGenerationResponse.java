package com.platform.universitygovernance.classgroup.presentation.dto;

import java.util.List;
import java.util.UUID;

public record ClassGroupGenerationResponse(
    UUID academicLevelId,
    UUID academicYearId,
    int totalStudents,
    int semesterAssignmentsCreated,
    List<GeneratedClassGroupResponse> groups
) {
}
