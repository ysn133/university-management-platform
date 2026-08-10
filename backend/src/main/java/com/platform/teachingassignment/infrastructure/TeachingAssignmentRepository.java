package com.platform.teachingassignment.infrastructure;

import com.platform.teachingassignment.domain.TeachingAssignment;
import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import com.platform.universitygovernance.semester.domain.SemesterTermType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    List<TeachingAssignment> findByTeachingRequirementTeachingGroupSemesterIdAndStatus(
        UUID semesterId,
        TeachingAssignmentStatus status
    );

    @Query("""
        select assignment
        from TeachingAssignment assignment
        join assignment.teachingRequirement requirement
        join requirement.teachingGroup teachingGroup
        join teachingGroup.semester semester
        where semester.academicYear.id = :academicYearId
          and semester.termType = :termType
          and semester.academicLevel.programFiliere.department.establishment.id = :establishmentId
          and assignment.status = :status
        """)
    List<TeachingAssignment> findInTeachingPeriod(
        @Param("establishmentId") UUID establishmentId,
        @Param("academicYearId") UUID academicYearId,
        @Param("termType") SemesterTermType termType,
        @Param("status") TeachingAssignmentStatus status
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
