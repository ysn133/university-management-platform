package com.platform.teachingassignment.presentation.dto;

import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import java.util.UUID;

public record UnresolvedTeachingRequirementResponse(
    UUID teachingRequirementId,
    UUID subjectModuleId,
    TeachingComponentType componentType,
    UUID teachingGroupId,
    String teachingGroupName,
    TeachingAssignmentUnresolvedReason reason,
    String message
) {
}
