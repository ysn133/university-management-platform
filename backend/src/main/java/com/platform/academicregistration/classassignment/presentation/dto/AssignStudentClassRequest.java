package com.platform.academicregistration.classassignment.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignStudentClassRequest(
    @NotNull UUID classGroupId
) {
}
