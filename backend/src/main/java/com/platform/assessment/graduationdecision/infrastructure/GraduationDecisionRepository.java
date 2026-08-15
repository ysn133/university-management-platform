package com.platform.assessment.graduationdecision.infrastructure;

import com.platform.assessment.graduationdecision.domain.GraduationDecision;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GraduationDecisionRepository extends JpaRepository<GraduationDecision, UUID> {
    Optional<GraduationDecision> findByTerminalAcademicRegistrationId(UUID registrationId);
    List<GraduationDecision> findByTerminalAcademicRegistrationIdIn(Collection<UUID> registrationIds);
    void deleteByTerminalAcademicRegistrationId(UUID registrationId);
}
