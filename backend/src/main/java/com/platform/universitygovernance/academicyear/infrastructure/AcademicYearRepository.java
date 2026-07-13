package com.platform.universitygovernance.academicyear.infrastructure;

import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {

    List<AcademicYear> findByEstablishmentIdOrderByStartYearDesc(UUID establishmentId);

    boolean existsByEstablishmentIdAndLabel(UUID establishmentId, String label);

    boolean existsByEstablishmentIdAndLabelAndIdNot(
        UUID establishmentId,
        String label,
        UUID academicYearId
    );
}
