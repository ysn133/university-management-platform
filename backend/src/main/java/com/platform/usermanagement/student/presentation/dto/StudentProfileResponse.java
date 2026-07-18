package com.platform.usermanagement.student.presentation.dto;

import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.shared.domain.Sex;
import java.time.LocalDate;
import java.util.UUID;

public record StudentProfileResponse(
    UUID studentId,
    UUID userAccountId,
    UUID establishmentId,
    String universityEmail,
    AccountRoleType roleType,
    AccountStatus accountStatus,
    String firstName,
    String lastName,
    LocalDate birthDate,
    Sex sex,
    String phoneNumber,
    String profilePicturePath
) {
}
