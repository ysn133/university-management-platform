package com.platform.usermanagement.superadmin.presentation.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.platform.shared.domain.Sex;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSuperAdminRequest(
    @NotBlank
    @Email
    String universityEmail,

    @NotBlank
    @Size(min = 8, max = 255)
    String password,

    @NotNull
    String firstName,

    @NotNull
    String lastName,

    @NotNull
    @DateTimeFormat
    @JsonProperty("birth_date")
    LocalDate birthDate,

    @NotNull
    Sex sex,

    @JsonProperty("phone_number")
    String phoneNumber
) {
}
