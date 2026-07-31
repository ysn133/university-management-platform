package com.platform.universitygovernance.subjectmodules.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record UpdateSubjectModuleRequest(
    @NotBlank @Size(max = 255) String code,
    @NotBlank @Size(max = 255) String title,
    @NotNull Set<UUID> academicDomainIds
) {

    public UpdateSubjectModuleRequest(String code, String title) {
        this(code, title, Set.of());
    }
}
