package com.platform.universitygovernance.subjectmodules.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record CreateSubjectModuleRequest(
    @NotBlank @Size(max = 255) String code,
    @NotBlank @Size(max = 255) String title,
    @NotNull Set<UUID> academicDomainIds
) {

    public CreateSubjectModuleRequest(String code, String title) {
        this(code, title, Set.of());
    }
}
