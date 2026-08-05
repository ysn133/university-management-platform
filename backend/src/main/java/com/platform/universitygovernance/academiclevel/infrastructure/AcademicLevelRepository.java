package com.platform.universitygovernance.academiclevel.infrastructure;

import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface AcademicLevelRepository extends JpaRepository<AcademicLevel, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select level from AcademicLevel level where level.id = :id")
    Optional<AcademicLevel> findByIdForUpdate(@Param("id") UUID id);

    List<AcademicLevel> findByProgramFiliereIdOrderByLevelOrderAsc(UUID programFiliereId);

    boolean existsByProgramFiliereIdAndNameIgnoreCase(UUID programFiliereId, String name);

    boolean existsByProgramFiliereIdAndNameIgnoreCaseAndIdNot(
        UUID programFiliereId,
        String name,
        UUID academicLevelId
    );

    boolean existsByProgramFiliereIdAndLevelOrder(UUID programFiliereId, int levelOrder);

    boolean existsByProgramFiliereIdAndLevelOrderAndIdNot(
        UUID programFiliereId,
        int levelOrder,
        UUID academicLevelId
    );
}
