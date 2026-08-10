package com.platform.usermanagement.professor.rank.infrastructure;

import com.platform.usermanagement.professor.rank.domain.AcademicRank;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicRankRepository extends JpaRepository<AcademicRank, UUID> {
    List<AcademicRank> findByEstablishmentIdOrderBySeniorityOrderAsc(UUID establishmentId);
    Optional<AcademicRank> findByEstablishmentIdAndNameIgnoreCase(UUID establishmentId, String name);
    boolean existsByEstablishmentIdAndCodeIgnoreCase(UUID establishmentId, String code);
    boolean existsByEstablishmentIdAndCodeIgnoreCaseAndIdNot(UUID establishmentId, String code, UUID id);
    boolean existsByEstablishmentIdAndNameIgnoreCase(UUID establishmentId, String name);
    boolean existsByEstablishmentIdAndNameIgnoreCaseAndIdNot(UUID establishmentId, String name, UUID id);
}
