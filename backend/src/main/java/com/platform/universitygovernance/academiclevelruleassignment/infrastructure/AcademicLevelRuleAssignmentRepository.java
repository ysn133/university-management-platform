package com.platform.universitygovernance.academiclevelruleassignment.infrastructure;

import com.platform.universitygovernance.academiclevelruleassignment.domain.AcademicLevelRuleAssignment;
import com.platform.universitygovernance.academiclevelruleassignment.domain.AcademicLevelRuleAssignmentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicLevelRuleAssignmentRepository
    extends JpaRepository<AcademicLevelRuleAssignment, UUID> {

    List<AcademicLevelRuleAssignment> findByAcademicLevelIdOrderByAcademicYearStartYearDesc(
        UUID academicLevelId
    );

    Optional<AcademicLevelRuleAssignment> findByAcademicLevelIdAndAcademicYearId(
        UUID academicLevelId,
        UUID academicYearId
    );

    boolean existsByAcademicLevelIdAndAcademicYearId(
        UUID academicLevelId,
        UUID academicYearId
    );

    boolean existsByAcademicLevelIdAndAcademicYearIdAndStatus(
        UUID academicLevelId,
        UUID academicYearId,
        AcademicLevelRuleAssignmentStatus status
    );

    boolean existsByAcademicRuleProfileId(UUID academicRuleProfileId);
}
