package com.platform.teachingassignment.infrastructure;

import com.platform.teachingassignment.domain.TeachingAssignment;
import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeachingAssignmentRepository
    extends JpaRepository<TeachingAssignment, UUID> {

    List<TeachingAssignment> findByProfessorEstablishmentIdOrderByCreatedAtDesc(
        UUID establishmentId
    );

    List<TeachingAssignment> findByProfessorIdOrderByCreatedAtDesc(UUID professorId);

    Optional<TeachingAssignment>
        findBySubjectModuleIdAndClassGroupIdAndAcademicYearIdAndSemesterIdAndStatus(
            UUID subjectModuleId,
            UUID classGroupId,
            UUID academicYearId,
            UUID semesterId,
            TeachingAssignmentStatus status
        );

    Optional<TeachingAssignment>
        findByProfessorIdAndSubjectModuleIdAndClassGroupIdAndAcademicYearIdAndSemesterId(
            UUID professorId,
            UUID subjectModuleId,
            UUID classGroupId,
            UUID academicYearId,
            UUID semesterId
        );
}
