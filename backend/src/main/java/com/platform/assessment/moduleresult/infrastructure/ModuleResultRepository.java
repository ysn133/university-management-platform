package com.platform.assessment.moduleresult.infrastructure;

import com.platform.assessment.moduleresult.domain.ModuleResult;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleResultRepository extends JpaRepository<ModuleResult, UUID> {

    Optional<ModuleResult> findByModuleRegistrationId(UUID moduleRegistrationId);

    List<ModuleResult> findByModuleRegistrationIdIn(Collection<UUID> moduleRegistrationIds);

    List<ModuleResult> findByModuleRegistrationSemesterRegestrationId(
        UUID semesterRegistrationId
    );

    List<ModuleResult> findByModuleRegistrationSemesterRegestrationAcademicRegistrationId(
        UUID academicRegistrationId
    );
}
