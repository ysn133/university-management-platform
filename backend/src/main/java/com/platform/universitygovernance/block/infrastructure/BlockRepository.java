package com.platform.universitygovernance.block.infrastructure;

import com.platform.universitygovernance.block.domain.Block;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<Block, UUID> {

    List<Block> findByEstablishmentIdOrderByCodeAsc(UUID establishmentId);

    boolean existsByEstablishmentIdAndCodeIgnoreCase(UUID establishmentId, String code);

    boolean existsByEstablishmentIdAndCodeIgnoreCaseAndIdNot(
        UUID establishmentId,
        String code,
        UUID blockId
    );
}
