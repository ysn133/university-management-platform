package com.platform.assessment.graderecord.presentation.dto;

import com.platform.assessment.graderecord.domain.GradeWorkflowStatus;
import java.util.List;
import java.util.UUID;

public record GradeSheetResponse(
    UUID moduleExamId,
    UUID subjectModuleId,
    UUID classGroupId,
    GradeWorkflowStatus workflowStatus,
    List<GradeItemResponse> grades
) {
}
