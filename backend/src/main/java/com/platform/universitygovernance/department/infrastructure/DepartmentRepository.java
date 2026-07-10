package com.platform.universitygovernance.department.infrastructure;

import com.platform.universitygovernance.department.domain.Department;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    List<Department> findByEstablishmentIdOrderByNameAsc(UUID establishmentId);

    boolean existsByEstablishmentIdAndNameIgnoreCase(UUID establishmentId, String name);

    boolean existsByEstablishmentIdAndNameIgnoreCaseAndIdNot(
        UUID establishmentId,
        String name,
        UUID departmentId
    );
}
