package com.platform.attendance.qrcheckin.infrastructure.redis;

import com.platform.attendance.qrcheckin.domain.AttendanceQrSession;
import com.platform.attendance.qrcheckin.infrastructure.AttendanceQrSessionStore;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class RedisAttendanceQrSessionStore implements AttendanceQrSessionStore {

    private static final String SESSION_PREFIX = "attendance:qr:session:";
    private static final String CHECK_INS_PREFIX = "attendance:qr:check-ins:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisAttendanceQrSessionStore(
        StringRedisTemplate redisTemplate,
        ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(AttendanceQrSession session, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(
                sessionKey(session.id()),
                objectMapper.writeValueAsString(session),
                ttl
            );
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to serialize attendance QR session", exception);
        }
    }

    @Override
    public Optional<AttendanceQrSession> find(UUID sessionId) {
        String payload = redisTemplate.opsForValue().get(sessionKey(sessionId));
        if (payload == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(payload, AttendanceQrSession.class));
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to deserialize attendance QR session", exception);
        }
    }

    @Override
    public void recordCheckIn(UUID sessionId, UUID studentId, Duration ttl) {
        String key = checkInsKey(sessionId);
        redisTemplate.opsForSet().add(key, studentId.toString());
        redisTemplate.expire(key, ttl);
    }

    @Override
    public Set<UUID> findCheckedInStudentIds(UUID sessionId) {
        Set<String> members = redisTemplate.opsForSet().members(checkInsKey(sessionId));
        if (members == null) {
            return Set.of();
        }
        return members.stream().map(UUID::fromString).collect(Collectors.toSet());
    }

    @Override
    public void delete(UUID sessionId) {
        redisTemplate.delete(Set.of(sessionKey(sessionId), checkInsKey(sessionId)));
    }

    private String sessionKey(UUID sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    private String checkInsKey(UUID sessionId) {
        return CHECK_INS_PREFIX + sessionId;
    }
}
