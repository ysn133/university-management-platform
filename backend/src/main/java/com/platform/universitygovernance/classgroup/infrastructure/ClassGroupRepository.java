package com.platform.universitygovernance.classgroup.infrastructure;

import com.platform.universitygovernance.classgroup.domain.ClassGroup;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassGroupRepository extends JpaRepository<ClassGroup, UUID> {

    List<ClassGroup> findByAcademicLevelIdAndAcademicYearIdOrderByNameAsc(
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
        UUID classGroupId
    );
}
