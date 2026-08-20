package com.platform.attendance.qrcheckin.presentation.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record AttendanceQrSessionResponse(
    UUID sessionId,
    UUID teachingAssignmentId,
    LocalDate attendanceDate,
    String token,
    Instant tokenExpiresAt,
    Instant closesAt,
    Set<UUID> checkedInStudentIds
) {
}
