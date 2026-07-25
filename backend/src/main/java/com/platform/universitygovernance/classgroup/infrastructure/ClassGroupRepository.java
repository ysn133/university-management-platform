package com.platform.universitygovernance.classgroup.infrastructure;

import com.platform.universitygovernance.classgroup.domain.ClassGroup;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassGroupRepository extends JpaRepository<ClassGroup, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select classGroup from ClassGroup classGroup where classGroup.id = :id")
    Optional<ClassGroup> findByIdForUpdate(@Param("id") UUID id);

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
