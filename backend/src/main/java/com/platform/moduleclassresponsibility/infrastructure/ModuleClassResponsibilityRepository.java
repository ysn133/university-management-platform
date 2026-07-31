package com.platform.moduleclassresponsibility.infrastructure;

import com.platform.moduleclassresponsibility.domain.ModuleClassResponsibility;
import com.platform.moduleclassresponsibility.domain.ModuleClassResponsibilityStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleClassResponsibilityRepository
    extends JpaRepository<ModuleClassResponsibility, UUID> {

    List<ModuleClassResponsibility> findByProfessorEstablishmentIdOrderByCreatedAtDesc(
        UUID establishmentId
    );

    List<ModuleClassResponsibility> findByProfessorIdOrderByCreatedAtDesc(UUID professorId);

    Optional<ModuleClassResponsibility>
        findBySubjectModuleIdAndClassGroupIdAndAcademicYearIdAndSemesterIdAndStatus(
            UUID subjectModuleId,
            UUID classGroupId,
            UUID academicYearId,
            UUID semesterId,
            ModuleClassResponsibilityStatus status
        );

    Optional<ModuleClassResponsibility>
        findByProfessorIdAndSubjectModuleIdAndClassGroupIdAndAcademicYearIdAndSemesterId(
            UUID professorId,
            UUID subjectModuleId,
            UUID classGroupId,
            UUID academicYearId,
            UUID semesterId
        );
}
