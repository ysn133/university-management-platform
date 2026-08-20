package com.platform.attendance.qrcheckin.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AttendanceQrSession(
    UUID id,
    UUID teachingAssignmentId,
    UUID professorId,
    LocalDate attendanceDate,
    String token,
    Instant tokenExpiresAt,
    Instant closesAt
) {
}
