package com.platform.universitygovernance.programfiliere.infrastructure;

import com.platform.universitygovernance.programfiliere.domain.ProgramFiliere;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramFiliereRepository extends JpaRepository<ProgramFiliere, UUID> {

    List<ProgramFiliere> findByDepartmentIdOrderByNameAsc(UUID departmentId);

    boolean existsByDepartmentIdAndDegreeCycleIdAndProgramPathIdAndCodeIgnoreCase(
        UUID departmentId,
        UUID degreeCycleId,
        UUID programPathId,
        String code
    );

    boolean existsByDepartmentIdAndDegreeCycleIdAndProgramPathIdAndCodeIgnoreCaseAndIdNot(
        UUID departmentId,
        UUID degreeCycleId,
        UUID programPathId,
        String code,
        UUID programFiliereId
    );
}
