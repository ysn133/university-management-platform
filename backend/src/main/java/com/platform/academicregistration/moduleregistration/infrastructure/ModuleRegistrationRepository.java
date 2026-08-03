package com.platform.academicregistration.moduleregistration.infrastructure;
import com.platform.academicregistration.moduleregistration.domain.ModuleRegistration;
import com.platform.academicregistration.moduleregistration.domain.ModuleRegistrationStatus;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ModuleRegistrationRepository extends JpaRepository<ModuleRegistration ,UUID>{

    List<ModuleRegistration> findBySemesterRegistrationIdOrderBySubjectModuleCodeAsc(
        UUID semesterRegistrationId
    );

    List<ModuleRegistration> findBySemesterRegistrationIdAndStatus(
        UUID semesterRegistrationId,
        ModuleRegistrationStatus status
    );

    @Query("""
        select registration
        from ModuleRegistration registration
        where registration.subjectModule.id = :subjectModuleId
          and registration.semesterRegistration.semester.id = :semesterId
          and registration.semesterRegistration.academicRegistration.academicYear.id = :academicYearId
          and registration.status = :status
          and exists (
              select assignment.id
              from StudentClassAssignment assignment
              where assignment.semesterRegistration.id = registration.semesterRegistration.id
                and assignment.classGroup.id = :classGroupId
          )
        """)
    List<ModuleRegistration> findEligibleForModuleExam(
        @Param("subjectModuleId") UUID subjectModuleId,
        @Param("classGroupId") UUID classGroupId,
        @Param("academicYearId") UUID academicYearId,
        @Param("semesterId") UUID semesterId,
        @Param("status") ModuleRegistrationStatus status
    );
}
