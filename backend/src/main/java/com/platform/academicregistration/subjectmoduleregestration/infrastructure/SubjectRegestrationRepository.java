package com.platform.academicregistration.subjectmoduleregestration.infrastructure;
import com.platform.academicregistration.subjectmoduleregestration.domain.SubjectModuleRegestration;
import com.platform.academicregistration.subjectmoduleregestration.domain.SubjectModuleRegistrationStatus;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubjectRegestrationRepository extends JpaRepository<SubjectModuleRegestration ,UUID>{

    @Query("""
        select registration
        from SubjectModuleRegestration registration
        where registration.subjectModule.id = :subjectModuleId
          and registration.semesterRegestration.semester.id = :semesterId
          and registration.semesterRegestration.academicRegistration.academicYear.id = :academicYearId
          and registration.status = :status
          and exists (
              select assignment.id
              from StudentClassAssignment assignment
              where assignment.semesterRegistration.id = registration.semesterRegestration.id
                and assignment.classGroup.id = :classGroupId
          )
        """)
    List<SubjectModuleRegestration> findEligibleForModuleExam(
        @Param("subjectModuleId") UUID subjectModuleId,
        @Param("classGroupId") UUID classGroupId,
        @Param("academicYearId") UUID academicYearId,
        @Param("semesterId") UUID semesterId,
        @Param("status") SubjectModuleRegistrationStatus status
    );
}
