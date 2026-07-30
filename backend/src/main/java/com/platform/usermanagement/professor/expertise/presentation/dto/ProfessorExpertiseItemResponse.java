package com.platform.usermanagement.professor.expertise.presentation.dto;

import java.util.UUID;

public record ProfessorExpertiseItemResponse(
    UUID academicDomainId,
    String code,
    String name
) {
}
