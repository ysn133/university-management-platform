package com.platform.academicregistration.classassignment.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BulkAssignStudentClassesRequest(
    @NotEmpty @Size(max = 2000)
    List<@Valid BulkClassAssignmentItemRequest> assignments
) {
}
