package com.platform.academicregistration.semesterregistration.infrastructer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.platform.academicregistration.semesterregistration.domain.SemesterRegestration;

public interface SemesterRegestrationRepository extends JpaRepository<SemesterRegestration, UUID> {

    Optional<SemesterRegestration> findByAcademicRegistrationIdAndSemesterId(
        UUID academicRegistrationId,
        UUID semesterId
    );

    List<SemesterRegestration> findByAcademicRegistrationId(
        UUID academicRegistrationId
    );

    List<SemesterRegestration> findBySemesterId(UUID semesterId);
}
