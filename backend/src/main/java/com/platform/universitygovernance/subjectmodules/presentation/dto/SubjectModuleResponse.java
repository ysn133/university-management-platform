package com.platform.universitygovernance.subjectmodules.presentation.dto;

import java.util.List;
import java.util.UUID;

public record SubjectModuleResponse(
    UUID id,
    UUID semesterId,
    String code,
    String title,
    List<UUID> academicDomainIds
) {
}
