package com.platform.scheduling.teachinggroup.infrastructure;

import com.platform.scheduling.teachinggroup.domain.TeachingGroupMembership;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeachingGroupMembershipRepository
    extends JpaRepository<TeachingGroupMembership, UUID> {

    List<TeachingGroupMembership> findByTeachingGroupIdIn(Collection<UUID> teachingGroupIds);
}
