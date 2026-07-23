package com.platform.usermanagement.professor.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.platform.shared.domain.Sex;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateProfessorRequest(
    @NotBlank @Email String universityEmail,
    @NotBlank @Size(min = 8, max = 255) String password,
    @NotBlank @Size(max = 255) String firstName,
    @NotBlank @Size(max = 255) String lastName,
    @NotNull @JsonProperty("birth_date") LocalDate birthDate,
    @NotNull Sex sex,
    @JsonProperty("phone_number") @Size(max = 50) String phoneNumber
) {
}
