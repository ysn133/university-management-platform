package com.platform.identityaccess.infrastructure;

import com.platform.identityaccess.domain.Student;
import com.platform.academicregistration.registration.domain.AcademicRegistrationStatus;
import com.platform.identityaccess.domain.AccountStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findByUserAccountId(UUID userAccountId);

    boolean existsByApogeeCodeIgnoreCase(String apogeeCode);

    boolean existsByApogeeCodeIgnoreCaseAndIdNot(String apogeeCode, UUID id);

    boolean existsByNationalStudentCodeIgnoreCase(String nationalStudentCode);

    boolean existsByNationalStudentCodeIgnoreCaseAndIdNot(String nationalStudentCode, UUID id);

    List<Student> findByEstablishmentIdOrderByCreatedAtAsc(UUID establishmentId);

    @Query("""
        select student
        from Student student
        join UserProfile profile on profile.userAccount.id = student.userAccount.id
        where student.establishment.id = :establishmentId
          and (:status is null or student.userAccount.accountStatus = :status)
          and (:enrolledFrom is null or student.initialEnrollmentDate >= :enrolledFrom)
          and (:enrolledTo is null or student.initialEnrollmentDate <= :enrolledTo)
          and (
            :query = ''
            or lower(profile.firstName) like concat('%', :query, '%')
            or lower(profile.lastName) like concat('%', :query, '%')
            or lower(concat(concat(profile.firstName, ' '), profile.lastName)) like concat('%', :query, '%')
            or lower(student.userAccount.universityEmail) like concat('%', :query, '%')
            or lower(student.apogeeCode) like concat('%', :query, '%')
            or lower(coalesce(student.nationalStudentCode, '')) like concat('%', :query, '%')
            or lower(coalesce(profile.cin, '')) like concat('%', :query, '%')
          )
          and (
            (:academicYearId is null and :programPathId is null and :programFiliereId is null
              and :academicLevelId is null and :semesterId is null and :registrationStatus is null)
            or exists (
              select registration.id
              from AcademicRegistration registration
              where registration.student = student
                and (:academicYearId is null or registration.academicYear.id = :academicYearId)
                and (:programPathId is null or registration.programFiliere.programPath.id = :programPathId)
                and (:programFiliereId is null or registration.programFiliere.id = :programFiliereId)
                and (:academicLevelId is null or registration.academicLevel.id = :academicLevelId)
                and (:registrationStatus is null or registration.status = :registrationStatus)
                and (:semesterId is null or exists (
                  select semesterRegistration.id
                  from SemesterRegistration semesterRegistration
                  where semesterRegistration.academicRegistration = registration
                    and semesterRegistration.semester.id = :semesterId
                ))
            )
          )
        order by lower(profile.lastName), lower(profile.firstName), student.id
        """)
    Page<Student> searchDirectory(
        @Param("establishmentId") UUID establishmentId,
        @Param("query") String query,
        @Param("status") AccountStatus status,
        @Param("enrolledFrom") LocalDate enrolledFrom,
        @Param("enrolledTo") LocalDate enrolledTo,
        @Param("academicYearId") UUID academicYearId,
        @Param("programPathId") UUID programPathId,
        @Param("programFiliereId") UUID programFiliereId,
        @Param("academicLevelId") UUID academicLevelId,
        @Param("semesterId") UUID semesterId,
        @Param("registrationStatus") AcademicRegistrationStatus registrationStatus,
        Pageable pageable
    );
}
