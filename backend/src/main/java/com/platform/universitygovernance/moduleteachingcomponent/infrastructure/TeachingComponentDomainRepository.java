package com.platform.universitygovernance.moduleteachingcomponent.infrastructure;

import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentDomain;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeachingComponentDomainRepository
    extends JpaRepository<TeachingComponentDomain, UUID> {

    List<TeachingComponentDomain> findByModuleTeachingComponentIdIn(
        Collection<UUID> moduleTeachingComponentIds
    );
}
