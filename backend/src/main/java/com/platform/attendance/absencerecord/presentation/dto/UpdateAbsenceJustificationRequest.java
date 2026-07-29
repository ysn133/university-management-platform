package com.platform.attendance.absencerecord.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAbsenceJustificationRequest(
    @NotNull Boolean justified,
    @Size(max = 1000) String justificationNote
) {
}
