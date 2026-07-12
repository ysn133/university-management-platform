package com.platform.universitygovernance.programpath.infrastructure;

import com.platform.universitygovernance.programpath.domain.ProgramPath;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramPathRepository extends JpaRepository<ProgramPath, UUID> {

    List<ProgramPath> findByEstablishmentIdOrderByNameAsc(UUID establishmentId);

    boolean existsByEstablishmentIdAndNameIgnoreCase(UUID establishmentId, String name);

    boolean existsByEstablishmentIdAndNameIgnoreCaseAndIdNot(
        UUID establishmentId,
        String name,
        UUID programPathId
    );
}
