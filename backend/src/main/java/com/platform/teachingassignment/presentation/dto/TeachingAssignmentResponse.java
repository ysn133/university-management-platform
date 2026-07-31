package com.platform.teachingassignment.presentation.dto;

import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import java.time.Instant;
import java.util.UUID;

public record TeachingAssignmentResponse(
    UUID id,
    UUID establishmentId,
    UUID professorId,
    UUID teachingRequirementId,
    UUID subjectModuleId,
    TeachingComponentType componentType,
    UUID teachingGroupId,
    String teachingGroupName,
    TeachingAssignmentStatus status,
    Instant createdAt,
    Instant updatedAt
) {
}
