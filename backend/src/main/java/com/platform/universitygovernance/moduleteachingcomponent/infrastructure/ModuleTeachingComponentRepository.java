package com.platform.universitygovernance.moduleteachingcomponent.infrastructure;

import com.platform.universitygovernance.moduleteachingcomponent.domain.ModuleTeachingComponent;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleTeachingComponentRepository
    extends JpaRepository<ModuleTeachingComponent, UUID> {

    List<ModuleTeachingComponent> findBySubjectModuleIdOrderByComponentTypeAsc(UUID subjectModuleId);

    List<ModuleTeachingComponent> findBySubjectModuleSemesterIdAndAudienceMode(
        UUID semesterId,
        TeachingAudienceMode audienceMode
    );

    List<ModuleTeachingComponent> findBySubjectModuleSemesterId(UUID semesterId);
}
