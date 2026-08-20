package com.platform.attendance.qrcheckin.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record AttendanceQrCheckInResponse(
    UUID sessionId,
    UUID studentId,
    Instant checkedInAt,
    String message
) {
}
