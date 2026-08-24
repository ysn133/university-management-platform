package com.platform.attendance.absencerecord.presentation.dto;

import com.platform.attendance.absencerecord.domain.AbsenceJustificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewAbsenceJustificationRequest(@NotNull AbsenceJustificationStatus decision, @Size(max = 1000) String note) {
}
