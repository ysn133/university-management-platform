package com.platform.scheduling.semesterschedule.infrastructure;

import com.platform.scheduling.semesterschedule.domain.SemesterSchedule;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SemesterScheduleRepository extends JpaRepository<SemesterSchedule, UUID> {

    List<SemesterSchedule> findByEstablishmentIdOrderByCreatedAtDesc(UUID establishmentId);

    boolean existsByEstablishmentIdAndAcademicYearIdAndSemesterId(
        UUID establishmentId,
        UUID academicYearId,
        UUID semesterId
    );
}
