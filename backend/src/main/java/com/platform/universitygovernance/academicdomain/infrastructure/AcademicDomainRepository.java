package com.platform.universitygovernance.academicdomain.infrastructure;

import com.platform.universitygovernance.academicdomain.domain.AcademicDomain;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicDomainRepository extends JpaRepository<AcademicDomain, UUID> {

    List<AcademicDomain> findByEstablishmentIdOrderByNameAsc(UUID establishmentId);

    boolean existsByEstablishmentIdAndCodeIgnoreCase(UUID establishmentId, String code);

    boolean existsByEstablishmentIdAndCodeIgnoreCaseAndIdNot(
        UUID establishmentId,
        String code,
        UUID academicDomainId
    );
}
