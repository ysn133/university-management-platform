package com.platform.identityaccess.infrastructure;

import com.platform.identityaccess.domain.UserProfile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByUserAccountId(UUID userAccountId);

    boolean existsByUserAccountId(UUID userAccountId);
}
