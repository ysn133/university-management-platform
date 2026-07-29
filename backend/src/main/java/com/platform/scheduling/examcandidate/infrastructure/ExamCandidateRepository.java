package com.platform.scheduling.examcandidate.infrastructure;

import com.platform.scheduling.examcandidate.domain.ExamCandidate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamCandidateRepository extends JpaRepository<ExamCandidate, UUID> {

    List<ExamCandidate> findByModuleExamIdOrderByCreatedAtAsc(UUID moduleExamId);

    @Query("""
        select candidate
        from ExamCandidate candidate
        where candidate.moduleRegistration.semesterRegestration.academicRegistration.student.id = :studentId
          and candidate.moduleExam.examSchedule.publicationStatus = com.platform.scheduling.examschedule.domain.PublicationStatus.PUBLISHED
        order by candidate.moduleExam.examDate asc, candidate.moduleExam.startTime asc
        """)
    List<ExamCandidate> findPublishedStudentInvitations(
        @Param("studentId") UUID studentId
    );
}
