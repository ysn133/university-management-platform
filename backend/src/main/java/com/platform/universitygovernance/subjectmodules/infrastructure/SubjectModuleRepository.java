package com.platform.universitygovernance.subjectmodules.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.platform.universitygovernance.subjectmodules.domain.SubjectModule;

public interface SubjectModuleRepository extends JpaRepository<SubjectModule, UUID> {

    List<SubjectModule> findBySemesterIdOrderByCodeAsc(UUID semesterId);

    boolean existsBySemesterIdAndCodeIgnoreCase(
        UUID semesterId,
        String code
    );

    boolean existsBySemesterIdAndCodeIgnoreCaseAndIdNot(
        UUID semesterId,
        String code,
        UUID subjectModuleId
    );
}
