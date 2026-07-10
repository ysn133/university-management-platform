package com.platform.identityaccess.infrastructure.redis;

import com.platform.identityaccess.domain.RefreshTokenSession;
import com.platform.identityaccess.infrastructure.RefreshTokenSessionStore;
import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class RedisRefreshTokenSessionStore implements RefreshTokenSessionStore {

    private static final String KEY_PREFIX = "refresh_token:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public RedisRefreshTokenSessionStore(
        StringRedisTemplate stringRedisTemplate,
        ObjectMapper objectMapper
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(RefreshTokenSession session, Duration ttl) {
        try {
            String payload = objectMapper.writeValueAsString(session);
            stringRedisTemplate.opsForValue().set(key(session.getTokenValue()), payload, ttl);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to serialize refresh token session", exception);
        }
    }

    @Override
    public Optional<RefreshTokenSession> findByToken(String tokenValue) {
        String payload = stringRedisTemplate.opsForValue().get(key(tokenValue));
        if (payload == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(payload, RefreshTokenSession.class));
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to deserialize refresh token session", exception);
        }
    }

    @Override
    public void delete(String tokenValue) {
        stringRedisTemplate.delete(key(tokenValue));
    }

    private String key(String tokenValue) {
        return KEY_PREFIX + tokenValue;
    }
}
