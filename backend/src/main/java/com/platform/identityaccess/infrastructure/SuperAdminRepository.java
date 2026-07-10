package com.platform.identityaccess.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.platform.identityaccess.domain.SuperAdmin;

public interface SuperAdminRepository extends JpaRepository<SuperAdmin, UUID> {

    Optional<SuperAdmin> findByUserAccountId(UUID userAccountId);

    List<SuperAdmin> findByEstablishmentId(UUID establishmentId);
}
