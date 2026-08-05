package com.platform.universitygovernance.classgroup.presentation.dto;

import java.util.List;
import java.util.UUID;

public record ClassGroupRebalanceResponse(
    UUID academicLevelId,
    UUID academicYearId,
    int totalStudents,
    int semesterAssignmentsChanged,
    List<GeneratedClassGroupResponse> groups
) {
}
