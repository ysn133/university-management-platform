package com.platform.usermanagement.professor.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.platform.shared.domain.Sex;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateProfessorRequest(
    @NotBlank @Size(max = 50) String employeeNumber,
    UUID academicRankId,
    @Size(max = 100) String academicRank,
    @PastOrPresent LocalDate hireDate,
    @NotNull @Positive Integer maximumWeeklyTeachingMinutes,
    @Size(max = 50) String cin,
    @NotBlank @Email String universityEmail,
    @NotBlank @Size(max = 255) String firstName,
    @NotBlank @Size(max = 255) String lastName,
    @NotNull @Past @JsonProperty("birth_date") LocalDate birthDate,
    @NotBlank @Size(max = 255) String placeOfBirth,
    @NotBlank @Size(max = 100) String nationality,
    @NotNull Sex sex,
    @JsonProperty("phone_number") @Size(max = 50) String phoneNumber
) {
}
