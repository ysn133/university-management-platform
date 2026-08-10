package com.platform.usermanagement.professor.rank.presentation.dto;

import com.platform.usermanagement.professor.rank.domain.AcademicRankStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AcademicRankRequest(
    @NotBlank @Size(max = 50) String code,
    @NotBlank @Size(max = 100) String name,
    @NotNull @Positive Integer seniorityOrder,
    boolean canHoldModuleResponsibility,
    @NotNull AcademicRankStatus status
) {}
