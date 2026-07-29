package com.platform.assessment.graderecord.infrastructure;

import com.platform.assessment.graderecord.domain.GradeRecord;
import com.platform.assessment.graderecord.domain.GradeWorkflowStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GradeRecordRepository extends JpaRepository<GradeRecord, UUID> {

    boolean existsByModuleExamId(UUID moduleExamId);

    List<GradeRecord> findByModuleExamId(UUID moduleExamId);

    Optional<GradeRecord> findByModuleExamIdAndModuleRegistrationId(
        UUID moduleExamId,
        UUID moduleRegistrationId
    );

    List<GradeRecord> findByModuleRegistrationIdAndWorkflowStatus(
        UUID moduleRegistrationId,
        GradeWorkflowStatus workflowStatus
    );

    @Query("""
        select grade
        from GradeRecord grade
        where grade.moduleRegistration.semesterRegestration.academicRegistration.student.id = :studentId
          and grade.workflowStatus = :status
          and (:academicYearId is null or grade.moduleRegistration.semesterRegestration.academicRegistration.academicYear.id = :academicYearId)
          and (:academicLevelId is null or grade.moduleRegistration.semesterRegestration.academicRegistration.academicLevel.id = :academicLevelId)
          and (:semesterId is null or grade.moduleRegistration.semesterRegestration.semester.id = :semesterId)
        order by grade.moduleRegistration.semesterRegestration.academicRegistration.academicYear.startYear desc,
                 grade.moduleRegistration.semesterRegestration.semester.semesterOrder asc
        """)
    List<GradeRecord> findStudentGrades(
        @Param("studentId") UUID studentId,
        @Param("status") GradeWorkflowStatus status,
        @Param("academicYearId") UUID academicYearId,
        @Param("academicLevelId") UUID academicLevelId,
        @Param("semesterId") UUID semesterId
    );
}
