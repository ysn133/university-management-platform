package com.platform.scheduling.examschedule.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.platform.scheduling.examschedule.domain.ExamSchedule;
import com.platform.scheduling.examschedule.domain.ExamSessionType;

public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, UUID> {

    List<ExamSchedule> findByEstablishmentIdOrderByCreatedAtDesc(UUID establishmentId);

    boolean existsByEstablishmentIdAndAcademicYearIdAndSemesterIdAndSessionType(
        UUID establishmentId,
        UUID academicYearId,
        UUID semesterId,
        ExamSessionType sessionType
    );

    boolean existsByEstablishmentIdAndAcademicYearIdAndSemesterIdAndSessionTypeAndIdNot(
        UUID establishmentId,
        UUID academicYearId,
        UUID semesterId,
        ExamSessionType sessionType,
        UUID examScheduleId
    );
}
