package com.platform.teachingassignment.infrastructure;

import com.platform.teachingassignment.domain.TeachingAssignment;
import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeachingAssignmentRepository
    extends JpaRepository<TeachingAssignment, UUID> {

    List<TeachingAssignment>
        findByTeachingRequirementTeachingGroupSemesterAcademicLevelProgramFiliereDepartmentEstablishmentIdOrderByCreatedAtDesc(
            UUID establishmentId
        );

    List<TeachingAssignment> findByProfessorIdOrderByCreatedAtDesc(UUID professorId);

    List<TeachingAssignment> findByProfessorIdAndStatus(
        UUID professorId,
        TeachingAssignmentStatus status
    );

    Optional<TeachingAssignment> findByTeachingRequirementIdAndStatus(
        UUID teachingRequirementId,
        TeachingAssignmentStatus status
    );

    Optional<TeachingAssignment> findByProfessorIdAndTeachingRequirementId(
        UUID professorId,
        UUID teachingRequirementId
    );
}
