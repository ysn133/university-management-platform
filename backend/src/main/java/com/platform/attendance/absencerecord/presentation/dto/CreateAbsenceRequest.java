package com.platform.attendance.absencerecord.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateAbsenceRequest(
    @NotNull UUID moduleRegistrationId,
    @NotNull LocalDate absenceDate
) {
}
