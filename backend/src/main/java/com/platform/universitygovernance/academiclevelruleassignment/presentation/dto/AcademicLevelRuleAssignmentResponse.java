package com.platform.universitygovernance.academiclevelruleassignment.presentation.dto;

import com.platform.universitygovernance.academiclevelruleassignment.domain.AcademicLevelRuleAssignmentStatus;
import java.time.Instant;
import java.util.UUID;

public record AcademicLevelRuleAssignmentResponse(
    UUID id,
    UUID academicLevelId,
    UUID academicYearId,
    UUID academicRuleProfileId,
    AcademicLevelRuleAssignmentStatus status,
    Instant createdAt,
    Instant updatedAt
) {
}
