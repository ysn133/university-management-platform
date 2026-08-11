package com.platform.scheduling.examgroup.infrastructure;

import com.platform.scheduling.examgroup.domain.ExamGroup;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamGroupRepository extends JpaRepository<ExamGroup, UUID> {
    List<ExamGroup> findByExamScheduleIdAndClassGroupIdOrderByGroupOrderAsc(UUID examScheduleId, UUID classGroupId);
}
