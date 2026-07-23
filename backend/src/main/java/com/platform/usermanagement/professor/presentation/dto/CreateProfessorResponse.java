package com.platform.usermanagement.professor.presentation.dto;

import com.platform.identityaccess.domain.AccountRoleType;
import java.util.UUID;

public record CreateProfessorResponse(
    UUID professorId,
    UUID userAccountId,
    UUID establishmentId,
    AccountRoleType roleType
) {
}
