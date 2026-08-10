package com.platform.identityaccess.infrastructure;

import com.platform.identityaccess.domain.Professor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, UUID> {
    boolean existsByAcademicRankId(UUID academicRankId);

    Optional<Professor> findByUserAccountId(UUID userAccountId);

    boolean existsByEmployeeNumberIgnoreCase(String employeeNumber);

    boolean existsByEmployeeNumberIgnoreCaseAndIdNot(String employeeNumber, UUID id);

    List<Professor> findByEstablishmentIdOrderByCreatedAtAsc(UUID establishmentId);
}
