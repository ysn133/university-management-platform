package com.platform.academicregistration.classassignment.presentation.dto;

import java.util.List;
import java.util.UUID;

public record ClassGroupRosterResponse(
    UUID academicLevelId,
    UUID academicYearId,
    UUID semesterId,
    int totalStudents,
    List<UUID> unassignedAcademicRegistrationIds,
    List<ClassGroupRosterGroupResponse> groups
) {
}
