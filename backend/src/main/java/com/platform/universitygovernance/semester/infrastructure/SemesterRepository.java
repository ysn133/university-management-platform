package com.platform.universitygovernance.semester.infrastructure;

import com.platform.universitygovernance.semester.domain.Semester;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SemesterRepository extends JpaRepository<Semester, UUID> {

    List<Semester> findByAcademicLevelIdAndAcademicYearIdOrderBySemesterOrderAsc(
        UUID academicLevelId,
        UUID academicYearId
    );

    boolean existsByAcademicLevelIdAndAcademicYearIdAndNameIgnoreCase(
        UUID academicLevelId,
        UUID academicYearId,
        String name
    );

    boolean existsByAcademicLevelIdAndAcademicYearIdAndNameIgnoreCaseAndIdNot(
        UUID academicLevelId,
        UUID academicYearId,
        String name,
        UUID semesterId
    );

    boolean existsByAcademicLevelIdAndAcademicYearIdAndSemesterOrder(
        UUID academicLevelId,
        UUID academicYearId,
        int semesterOrder
    );

    boolean existsByAcademicLevelIdAndAcademicYearIdAndSemesterOrderAndIdNot(
        UUID academicLevelId,
        UUID academicYearId,
        int semesterOrder,
        UUID semesterId
    );
}
