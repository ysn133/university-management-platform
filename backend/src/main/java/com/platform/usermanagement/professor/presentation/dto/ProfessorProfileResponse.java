package com.platform.usermanagement.professor.presentation.dto;

import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.shared.domain.Sex;
import java.time.LocalDate;
import java.util.UUID;

public record ProfessorProfileResponse(
    UUID professorId,
    UUID userAccountId,
    UUID establishmentId,
    String employeeNumber,
    String academicRank,
    LocalDate hireDate,
    Integer maximumWeeklyTeachingMinutes,
    String universityEmail,
    AccountRoleType roleType,
    AccountStatus accountStatus,
    String firstName,
    String lastName,
    LocalDate birthDate,
    String placeOfBirth,
    String nationality,
    String cin,
    Sex sex,
    String phoneNumber,
    String profilePicturePath
) {
}
