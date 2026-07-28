package com.platform.assessment.progressiondecision.infrastructure;

import com.platform.assessment.progressiondecision.domain.ProgressionDecision;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressionDecisionRepository
    extends JpaRepository<ProgressionDecision, UUID> {

    Optional<ProgressionDecision> findByAcademicRegistrationId(
        UUID academicRegistrationId
    );
}
