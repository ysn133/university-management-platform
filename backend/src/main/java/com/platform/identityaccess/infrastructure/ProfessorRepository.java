package com.platform.identityaccess.infrastructure;

import com.platform.identityaccess.domain.Professor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, UUID> {

    Optional<Professor> findByUserAccountId(UUID userAccountId);

    List<Professor> findByEstablishmentIdOrderByCreatedAtAsc(UUID establishmentId);
}
