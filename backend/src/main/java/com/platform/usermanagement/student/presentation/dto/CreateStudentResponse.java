package com.platform.usermanagement.student.presentation.dto;

import com.platform.identityaccess.domain.AccountRoleType;
import java.util.UUID;

public record CreateStudentResponse(
    UUID studentId,
    UUID userAccountId,
    UUID establishmentId,
    AccountRoleType roleType
) {
}
