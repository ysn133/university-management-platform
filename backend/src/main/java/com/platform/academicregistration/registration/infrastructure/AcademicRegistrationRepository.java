package com.platform.academicregistration.registration.infrastructure;

import com.platform.academicregistration.registration.domain.AcademicRegistration;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicRegistrationRepository extends JpaRepository<AcademicRegistration, UUID> {

    boolean existsByStudentIdAndAcademicYearId(UUID studentId, UUID academicYearId);

    List<AcademicRegistration> findByStudentEstablishmentIdOrderByAcademicYearStartYearDesc(
        UUID establishmentId
    );

    List<AcademicRegistration> findByStudentIdOrderByAcademicYearStartYearDesc(UUID studentId);

}
