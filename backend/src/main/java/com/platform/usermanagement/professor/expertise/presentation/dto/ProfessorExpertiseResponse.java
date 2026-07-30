package com.platform.usermanagement.professor.expertise.presentation.dto;

import java.util.List;
import java.util.UUID;

public record ProfessorExpertiseResponse(
    UUID professorId,
    List<ProfessorExpertiseItemResponse> academicDomains
) {
}
