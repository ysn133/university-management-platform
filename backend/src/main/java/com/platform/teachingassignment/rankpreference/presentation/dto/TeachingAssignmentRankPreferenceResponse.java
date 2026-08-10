package com.platform.teachingassignment.rankpreference.presentation.dto;

import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import java.util.UUID;

public record TeachingAssignmentRankPreferenceResponse(
    UUID id,
    UUID establishmentId,
    TeachingComponentType componentType,
    UUID academicRankId,
    String academicRankCode,
    String academicRankName,
    int priority
) {}
