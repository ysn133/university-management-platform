package com.platform.usermanagement.professor.expertise.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record ReplaceProfessorExpertiseRequest(
    @NotNull Set<UUID> academicDomainIds
) {
}
