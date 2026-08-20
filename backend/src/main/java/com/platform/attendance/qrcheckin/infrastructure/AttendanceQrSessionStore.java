package com.platform.attendance.qrcheckin.infrastructure;

import com.platform.attendance.qrcheckin.domain.AttendanceQrSession;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AttendanceQrSessionStore {

    void save(AttendanceQrSession session, Duration ttl);

    Optional<AttendanceQrSession> find(UUID sessionId);

    void recordCheckIn(UUID sessionId, UUID studentId, Duration ttl);

    Set<UUID> findCheckedInStudentIds(UUID sessionId);

    void delete(UUID sessionId);
}
