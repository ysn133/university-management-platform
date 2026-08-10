package com.platform.teachingassignment.presentation.dto;

import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import com.platform.teachingassignment.domain.TeachingAssignmentSource;
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
    TeachingAssignmentSource assignmentSource,
    Instant createdAt,
    Instant updatedAt
) {
}
