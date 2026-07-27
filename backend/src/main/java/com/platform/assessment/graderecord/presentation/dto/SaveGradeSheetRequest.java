package com.platform.assessment.graderecord.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SaveGradeSheetRequest(
    @NotEmpty List<@Valid GradeItemRequest> grades
) {
}
