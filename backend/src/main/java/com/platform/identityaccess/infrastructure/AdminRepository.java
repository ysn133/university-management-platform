package com.platform.identityaccess.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.platform.identityaccess.domain.Admin;

public interface AdminRepository extends JpaRepository<Admin, UUID> {

    Optional<Admin> findByUserAccountId(UUID userAccountId);

    List<Admin> findByEstablishmentId(UUID establishmentId);
}
