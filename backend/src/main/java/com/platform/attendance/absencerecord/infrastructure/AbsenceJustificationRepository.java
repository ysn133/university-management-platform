package com.platform.attendance.absencerecord.infrastructure;

import com.platform.attendance.absencerecord.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbsenceJustificationRepository extends JpaRepository<AbsenceJustification, UUID> {
    boolean existsByAbsenceRecordIdAndStatus(UUID absenceId, AbsenceJustificationStatus status);
    List<AbsenceJustification> findBySubmittedByIdOrderBySubmittedAtDesc(UUID studentId);
    List<AbsenceJustification> findByAbsenceRecordTeachingAssignmentIdOrderBySubmittedAtDesc(UUID teachingAssignmentId);
}
