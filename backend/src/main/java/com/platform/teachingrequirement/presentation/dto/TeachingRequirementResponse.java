package com.platform.teachingrequirement.presentation.dto;

import com.platform.teachingrequirement.domain.TeachingRequirementStatus;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import java.util.UUID;

public record TeachingRequirementResponse(
    UUID id,
    UUID subjectModuleId,
    UUID moduleTeachingComponentId,
    TeachingComponentType componentType,
    UUID teachingGroupId,
    String teachingGroupName,
    UUID sourceClassGroupId,
    String sourceClassGroupName,
    TeachingAudienceMode audienceType,
    TeachingRequirementStatus status
) {
}
