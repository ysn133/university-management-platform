package com.platform.identityaccess.infrastructure;

import com.platform.identityaccess.domain.RefreshTokenSession;
import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenSessionStore {

    void save(RefreshTokenSession session, Duration ttl);

    Optional<RefreshTokenSession> findByToken(String tokenValue);

    void delete(String tokenValue);
}