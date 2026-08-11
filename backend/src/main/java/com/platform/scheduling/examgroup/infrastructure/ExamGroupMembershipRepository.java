package com.platform.scheduling.examgroup.infrastructure;

import com.platform.scheduling.examgroup.domain.ExamGroupMembership;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExamGroupMembershipRepository extends JpaRepository<ExamGroupMembership, UUID> {
    long countByExamGroupId(UUID examGroupId);
    List<ExamGroupMembership> findByExamGroupIdIn(Collection<UUID> examGroupIds);
    Optional<ExamGroupMembership> findBySemesterRegistrationIdAndExamGroupExamScheduleId(UUID semesterRegistrationId, UUID examScheduleId);
}
