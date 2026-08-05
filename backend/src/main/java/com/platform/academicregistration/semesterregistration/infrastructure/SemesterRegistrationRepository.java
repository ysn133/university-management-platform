package com.platform.academicregistration.semesterregistration.infrastructure;

import java.util.List;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.platform.academicregistration.semesterregistration.domain.SemesterRegistration;

public interface SemesterRegistrationRepository extends JpaRepository<SemesterRegistration, UUID> {

    Optional<SemesterRegistration> findByAcademicRegistrationIdAndSemesterId(
        UUID academicRegistrationId,
        UUID semesterId
    );

    List<SemesterRegistration> findByAcademicRegistrationId(
        UUID academicRegistrationId
    );

    List<SemesterRegistration> findBySemesterId(UUID semesterId);

    List<SemesterRegistration> findByAcademicRegistrationIdIn(
        Collection<UUID> academicRegistrationIds
    );
}
