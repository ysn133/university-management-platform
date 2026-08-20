package com.platform.attendance.absencerecord.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record ConfirmAttendanceRequest(
    @NotNull LocalDate attendanceDate,
    @NotNull Set<@NotNull UUID> absentStudentIds
) {
}
