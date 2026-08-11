package com.platform.scheduling.examgroup.infrastructure;

import com.platform.scheduling.examgroup.domain.ExamRoomAllocation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRoomAllocationRepository extends JpaRepository<ExamRoomAllocation, UUID> {
    List<ExamRoomAllocation> findByModuleExamIdOrderByExamGroupGroupOrderAsc(UUID moduleExamId);
    List<ExamRoomAllocation> findByRoomId(UUID roomId);
    long countByModuleExamId(UUID moduleExamId);
    Optional<ExamRoomAllocation> findByModuleExamIdAndExamGroupId(UUID moduleExamId, UUID examGroupId);
    void deleteByModuleExamId(UUID moduleExamId);
}
