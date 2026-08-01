package com.platform.usermanagement.admin.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.platform.shared.domain.Sex;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateAdminRequest(
    @NotBlank @Email String universityEmail,
    @NotBlank @Size(max = 255) String firstName,
    @NotBlank @Size(max = 255) String lastName,
    @NotNull @Past @JsonProperty("birth_date") LocalDate birthDate,
    @Size(max = 50) String cin,
    @NotNull Sex sex,
    @JsonProperty("phone_number") @Size(max = 50) String phoneNumber
) {
}
