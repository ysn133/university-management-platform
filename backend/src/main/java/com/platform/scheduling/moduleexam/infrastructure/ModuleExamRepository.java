package com.platform.scheduling.moduleexam.infrastructure;

import com.platform.scheduling.moduleexam.domain.ModuleExam;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ModuleExamRepository extends JpaRepository<ModuleExam, UUID> {

    @Query("""
        select exam
        from ModuleExam exam, ModuleClassResponsibility responsibility
        where responsibility.professor.id = :professorId
          and responsibility.subjectModule.id = exam.subjectModule.id
          and responsibility.classGroup.id = exam.classGroup.id
          and responsibility.academicYear.id = exam.examSchedule.academicYear.id
          and responsibility.semester.id = exam.examSchedule.semester.id
          and responsibility.status = com.platform.moduleclassresponsibility.domain.ModuleClassResponsibilityStatus.ACTIVE
          and exam.examSchedule.publicationStatus = com.platform.scheduling.examschedule.domain.PublicationStatus.PUBLISHED
        order by exam.examDate asc, exam.startTime asc, exam.subjectModule.title asc
        """)
    List<ModuleExam> findPublishedResponsibleExams(@Param("professorId") UUID professorId);

    List<ModuleExam> findByExamScheduleIdOrderByExamDateAscStartTimeAsc(
        UUID examScheduleId
    );

    List<ModuleExam> findByExamScheduleIdAndClassGroupIdAndExamDate(
        UUID examScheduleId,
        UUID classGroupId,
        LocalDate examDate
    );

    List<ModuleExam> findByRoomIdAndExamDate(UUID roomId, LocalDate examDate);

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
