package com.platform.universitygovernance.academiclevelruleassignment.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateAcademicLevelRuleAssignmentRequest(
    @NotNull UUID academicYearId,
    @NotNull UUID academicRuleProfileId
) {
}
