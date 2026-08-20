package com.platform.attendance.qrcheckin.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record StartAttendanceQrSessionRequest(
    @NotNull LocalDate attendanceDate
) {
}
