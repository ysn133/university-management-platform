package com.platform.universitygovernance.academiclevelruleassignment.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UpdateAcademicLevelRuleAssignmentRequest(
    @NotNull UUID academicRuleProfileId
) {
}
