package com.platform.scheduling.teachinggroup.infrastructure;

import com.platform.scheduling.teachinggroup.domain.TeachingGroup;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeachingGroupRepository extends JpaRepository<TeachingGroup, UUID> {

    List<TeachingGroup> findBySemesterIdOrderByAudienceTypeAscNameAsc(UUID semesterId);
}
