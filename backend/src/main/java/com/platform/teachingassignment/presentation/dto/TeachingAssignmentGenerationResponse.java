package com.platform.teachingassignment.presentation.dto;

import java.util.List;
import java.util.UUID;

public record TeachingAssignmentGenerationResponse(
    UUID semesterId,
    int preservedAssignmentCount,
    List<TeachingAssignmentResponse> createdAssignments,
    List<UnresolvedTeachingRequirementResponse> unresolvedRequirements,
    List<ProfessorTeachingWorkloadResponse> professorWorkloads
) {
}
