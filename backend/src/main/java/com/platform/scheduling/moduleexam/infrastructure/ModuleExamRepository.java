package com.platform.scheduling.moduleexam.infrastructure;

import com.platform.scheduling.moduleexam.domain.ModuleExam;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleExamRepository extends JpaRepository<ModuleExam, UUID> {

    List<ModuleExam> findByExamScheduleIdOrderByExamDateAscStartTimeAsc(
        UUID examScheduleId
    );

    List<ModuleExam> findByExamScheduleIdAndClassGroupIdAndExamDate(
        UUID examScheduleId,
        UUID classGroupId,
        LocalDate examDate
    );

    boolean existsByExamScheduleIdAndSubjectModuleIdAndClassGroupId(
        UUID examScheduleId,
        UUID subjectModuleId,
        UUID classGroupId
    );

    boolean existsByExamScheduleIdAndSubjectModuleIdAndClassGroupIdAndIdNot(
        UUID examScheduleId,
        UUID subjectModuleId,
        UUID classGroupId,
        UUID moduleExamId
    );
}
