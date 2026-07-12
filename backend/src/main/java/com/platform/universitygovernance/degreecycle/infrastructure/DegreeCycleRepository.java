package com.platform.universitygovernance.degreecycle.infrastructure;

import com.platform.universitygovernance.degreecycle.domain.DegreeCycle;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DegreeCycleRepository extends JpaRepository<DegreeCycle, UUID> {

    List<DegreeCycle> findByEstablishmentIdOrderByNameAsc(UUID establishmentId);

    boolean existsByEstablishmentIdAndNameIgnoreCase(UUID establishmentId, String name);

    boolean existsByEstablishmentIdAndNameIgnoreCaseAndIdNot(
        UUID establishmentId,
        String name,
        UUID degreeCycleId
    );
}
