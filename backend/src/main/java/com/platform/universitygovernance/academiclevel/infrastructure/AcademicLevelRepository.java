package com.platform.universitygovernance.academiclevel.infrastructure;

import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicLevelRepository extends JpaRepository<AcademicLevel, UUID> {

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
