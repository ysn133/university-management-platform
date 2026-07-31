package com.platform.teachingrequirement.infrastructure;

import com.platform.teachingrequirement.domain.TeachingRequirement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeachingRequirementRepository
    extends JpaRepository<TeachingRequirement, UUID> {

    List<TeachingRequirement> findByTeachingGroupSemesterIdOrderByCreatedAtAsc(
        UUID semesterId
    );

    Optional<TeachingRequirement>
        findByModuleTeachingComponentIdAndTeachingGroupId(
            UUID moduleTeachingComponentId,
            UUID teachingGroupId
        );

    boolean existsByTeachingGroupSemesterId(UUID semesterId);
}
