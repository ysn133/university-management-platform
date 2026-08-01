package com.platform.attendance.absencerecord.infrastructure;

import com.platform.attendance.absencerecord.domain.AbsenceRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbsenceRecordRepository extends JpaRepository<AbsenceRecord, UUID> {

    boolean existsByModuleRegistrationIdAndTeachingAssignmentIdAndAbsenceDate(
        UUID moduleRegistrationId,
        UUID teachingAssignmentId,
        LocalDate absenceDate
    );

    long countByModuleRegistrationIdAndJustifiedFalse(UUID moduleRegistrationId);

    List<AbsenceRecord> findByTeachingAssignmentIdOrderByAbsenceDateDesc(
        UUID teachingAssignmentId
    );

    List<AbsenceRecord> findByModuleRegistrationSemesterRegistrationAcademicRegistrationStudentIdOrderByAbsenceDateDesc(
        UUID studentId
    );
}
