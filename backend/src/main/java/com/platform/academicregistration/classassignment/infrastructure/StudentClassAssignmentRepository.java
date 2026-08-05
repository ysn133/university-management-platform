package com.platform.academicregistration.classassignment.infrastructure;

import com.platform.academicregistration.classassignment.domain.StudentClassAssignment;
import java.util.List;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentClassAssignmentRepository
    extends JpaRepository<StudentClassAssignment, UUID> {

    Optional<StudentClassAssignment> findBySemesterRegistrationId(
        UUID semesterRegistrationId
    );

    List<StudentClassAssignment> findBySemesterRegistrationSemesterId(UUID semesterId);

    List<StudentClassAssignment> findBySemesterRegistrationIdIn(
        Collection<UUID> semesterRegistrationIds
    );
}
