package com.platform.usermanagement.superadmin.presentation.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.platform.shared.domain.Sex;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record CreateSuperAdminRequest(
    @NotBlank
    @Email
    String universityEmail,

    @NotBlank
    @Size(min = 8, max = 255)
    String password,

    @NotBlank
    @Size(max = 255)
    String firstName,

    @NotBlank
    @Size(max = 255)
    String lastName,

    @NotNull
    @Past
    @DateTimeFormat
    @JsonProperty("birth_date")
    LocalDate birthDate,

    @Size(max = 50)
    String cin,

    @NotNull
    Sex sex,

    @JsonProperty("phone_number")
    @Size(max = 50)
    String phoneNumber
) {
}
