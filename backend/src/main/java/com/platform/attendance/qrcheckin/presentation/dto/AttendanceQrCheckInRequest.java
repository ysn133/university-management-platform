package com.platform.attendance.qrcheckin.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AttendanceQrCheckInRequest(
    @NotNull UUID sessionId,
    @NotBlank String token
) {
}
