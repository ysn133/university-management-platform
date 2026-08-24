package com.platform.attendance.absencerecord.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SubmitAbsenceJustificationRequest(@NotBlank @Size(max = 1500) String reason, @NotNull UUID documentId) {
}
