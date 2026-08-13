package com.platform.assessment.semesterresult.infrastructure;

import com.platform.assessment.semesterresult.domain.SemesterResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SemesterResultRepository extends JpaRepository<SemesterResult, UUID> {

    Optional<SemesterResult> findBySemesterRegistrationId(UUID semesterRegistrationId);

    List<SemesterResult> findBySemesterRegistrationAcademicRegistrationId(
        UUID academicRegistrationId
    );

    List<SemesterResult> findBySemesterRegistrationIdIn(List<UUID> semesterRegistrationIds);
}
