package com.platform.identityaccess.infrastructure;

import com.platform.identityaccess.domain.RootSuperAdmin;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RootSuperAdminRepository extends JpaRepository<RootSuperAdmin, UUID> {

    long count();

    Optional<RootSuperAdmin> findByUserAccountId(UUID userAccountId);

    boolean existsByUserAccountId(UUID userAccountId);
}