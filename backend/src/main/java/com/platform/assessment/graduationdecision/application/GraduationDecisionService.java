package com.platform.assessment.graduationdecision.application;

import com.platform.academicregistration.registration.domain.AcademicRegistration;
import com.platform.academicregistration.registration.domain.AcademicRegistrationStatus;
import com.platform.academicregistration.registration.infrastructure.AcademicRegistrationRepository;
import com.platform.assessment.graduationdecision.domain.GraduationDecision;
import com.platform.assessment.graduationdecision.domain.GraduationDecisionStatus;
import com.platform.assessment.graduationdecision.infrastructure.GraduationDecisionRepository;
import com.platform.assessment.graduationdecision.presentation.dto.GraduationDecisionResponse;
import com.platform.assessment.progressiondecision.domain.ProgressionDecision;
import com.platform.assessment.progressiondecision.domain.ProgressionDecisionStatus;
import com.platform.assessment.progressiondecision.infrastructure.ProgressionDecisionRepository;
import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.UserProfile;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academiclevel.infrastructure.AcademicLevelRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GraduationDecisionService {

    private static final Set<ProgressionDecisionStatus> COMPLETED_LEVEL_STATUSES = EnumSet.of(
        ProgressionDecisionStatus.PROMOTED,
        ProgressionDecisionStatus.PROMOTED_BY_COMPENSATION,
        ProgressionDecisionStatus.PROMOTED_WITH_DEBT,
        ProgressionDecisionStatus.LEVEL_VALIDATED
    );

    private final AcademicRegistrationRepository registrationRepository;
    private final AcademicLevelRepository academicLevelRepository;
    private final ProgressionDecisionRepository progressionDecisionRepository;
    private final GraduationDecisionRepository graduationDecisionRepository;
    private final UserProfileRepository profileRepository;
    private final AdminPermissionAuthorizationService authorizationService;

    public GraduationDecisionService(
        AcademicRegistrationRepository registrationRepository,
        AcademicLevelRepository academicLevelRepository,
        ProgressionDecisionRepository progressionDecisionRepository,
        GraduationDecisionRepository graduationDecisionRepository,
        UserProfileRepository profileRepository,
        AdminPermissionAuthorizationService authorizationService
    ) {
        this.registrationRepository = registrationRepository;
        this.academicLevelRepository = academicLevelRepository;
        this.progressionDecisionRepository = progressionDecisionRepository;
        this.graduationDecisionRepository = graduationDecisionRepository;
        this.profileRepository = profileRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public List<GraduationDecisionResponse> get(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId,
        UUID academicYearId
    ) {
        List<AcademicRegistration> registrations = terminalRegistrations(
            principal,
            academicLevelId,
            academicYearId
        );
        Set<UUID> registrationIds = registrations.stream()
            .map(AcademicRegistration::getId)
            .collect(java.util.stream.Collectors.toSet());
        return graduationDecisionRepository
            .findByTerminalAcademicRegistrationIdIn(registrationIds)
            .stream()
            .map(this::response)
            .sorted((left, right) -> left.apogeeCode().compareToIgnoreCase(right.apogeeCode()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<GraduationDecisionResponse> getMine(
        AuthenticatedUserPrincipal principal
    ) {
        if (principal == null || principal.role() != AccountRoleType.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
        }
        Set<UUID> registrationIds = registrationRepository
            .findByStudentIdOrderByAcademicYearStartYearDesc(principal.roleEntityId())
            .stream()
            .map(AcademicRegistration::getId)
            .collect(java.util.stream.Collectors.toSet());
        return graduationDecisionRepository
            .findByTerminalAcademicRegistrationIdIn(registrationIds)
            .stream()
            .map(this::response)
            .sorted((left, right) -> right.decidedAt().compareTo(left.decidedAt()))
            .toList();
    }

    @Transactional
    public List<GraduationDecisionResponse> generate(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId,
        UUID academicYearId
    ) {
        List<AcademicRegistration> registrations = terminalRegistrations(
            principal,
            academicLevelId,
            academicYearId
        );
        registrations.forEach(this::recalculate);
        return get(principal, academicLevelId, academicYearId);
    }

    @Transactional
    public void recalculateExistingAfterHistoricalResultChange(
        AcademicRegistration affectedRegistration
    ) {
        graduationDecisionRepository
            .findByTerminalAcademicRegistrationStudentIdAndTerminalAcademicRegistrationProgramFiliereId(
                affectedRegistration.getStudent().getId(),
                affectedRegistration.getProgramFiliere().getId()
            )
            .ifPresent(decision -> recalculate(
                decision.getTerminalAcademicRegistration()
            ));
    }

    private void recalculate(AcademicRegistration terminalRegistration) {
        Optional<List<ProgressionDecision>> completedDecisions = completedProgramDecisions(
            terminalRegistration
        );
        if (completedDecisions.isEmpty()) {
            graduationDecisionRepository.deleteByTerminalAcademicRegistrationId(
                terminalRegistration.getId()
            );
            return;
        }

        List<ProgressionDecision> decisions = completedDecisions.get();
        BigDecimal average = decisions.stream()
            .map(ProgressionDecision::getAnnualAverage)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(decisions.size()), 2, RoundingMode.HALF_UP);
        GraduationDecision graduationDecision = graduationDecisionRepository
            .findByTerminalAcademicRegistrationId(terminalRegistration.getId())
            .orElseGet(GraduationDecision::new);
        graduationDecision.setTerminalAcademicRegistration(terminalRegistration);
        graduationDecision.setDecisionStatus(GraduationDecisionStatus.GRADUATED);
        graduationDecision.setGraduationAverage(average);
        graduationDecision.setDecidedAt(Instant.now());
        graduationDecisionRepository.save(graduationDecision);
    }

    private Optional<List<ProgressionDecision>> completedProgramDecisions(
        AcademicRegistration terminalRegistration
    ) {
        ProgressionDecision terminalDecision = progressionDecisionRepository
            .findByAcademicRegistrationId(terminalRegistration.getId())
            .orElse(null);
        if (terminalDecision == null
            || terminalDecision.getDecisionStatus() != ProgressionDecisionStatus.LEVEL_VALIDATED) {
            return Optional.empty();
        }

        UUID programId = terminalRegistration.getProgramFiliere().getId();
        List<AcademicLevel> requiredLevels = academicLevelRepository
            .findByProgramFiliereIdOrderByLevelOrderAsc(programId);
        List<AcademicRegistration> history = registrationRepository
            .findByStudentIdAndProgramFiliereIdOrderByAcademicYearStartYearDesc(
                terminalRegistration.getStudent().getId(),
                programId
            );
        List<ProgressionDecision> decisions = requiredLevels.stream()
            .map(level -> latestDecisionForLevel(history, level.getId()).orElse(null))
            .toList();
        if (decisions.stream().anyMatch(decision -> decision == null
            || !COMPLETED_LEVEL_STATUSES.contains(decision.getDecisionStatus()))) {
            return Optional.empty();
        }
        return Optional.of(decisions);
    }

    private Optional<ProgressionDecision> latestDecisionForLevel(
        List<AcademicRegistration> history,
        UUID academicLevelId
    ) {
        return history.stream()
            .filter(registration -> registration.getAcademicLevel().getId().equals(academicLevelId))
            .map(registration -> progressionDecisionRepository
                .findByAcademicRegistrationId(registration.getId()).orElse(null))
            .filter(java.util.Objects::nonNull)
            .findFirst();
    }

    private List<AcademicRegistration> terminalRegistrations(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId,
        UUID academicYearId
    ) {
        AcademicLevel level = academicLevelRepository.findById(academicLevelId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic level not found"
            ));
        if (!level.isTerminalLevel()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Graduation decisions are available only for a terminal academic level"
            );
        }
        UUID establishmentId = level.getProgramFiliere().getDepartment().getEstablishment().getId();
        authorizationService.requirePermission(principal, establishmentId, PermissionCode.GRADE_VIEW);
        return registrationRepository
            .findByAcademicLevelIdAndAcademicYearIdAndStatusOrderByStudentApogeeCodeAsc(
                academicLevelId,
                academicYearId,
                AcademicRegistrationStatus.ACTIVE
            );
    }

    private GraduationDecisionResponse response(GraduationDecision decision) {
        AcademicRegistration registration = decision.getTerminalAcademicRegistration();
        UserProfile profile = profileRepository
            .findByUserAccountId(registration.getStudent().getUserAccount().getId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Student profile not found"
            ));
        return new GraduationDecisionResponse(
            decision.getId(),
            registration.getStudent().getId(),
            profile.getFirstName(),
            profile.getLastName(),
            registration.getStudent().getApogeeCode(),
            registration.getStudent().getNationalStudentCode(),
            profile.getCin(),
            registration.getProgramFiliere().getName(),
            registration.getProgramFiliere().getProgramPath().getName(),
            registration.getProgramFiliere().getDegreeCycle().getName(),
            registration.getAcademicLevel().getName(),
            registration.getAcademicYear().getLabel(),
            decision.getDecisionStatus(),
            decision.getGraduationAverage(),
            decision.getDecidedAt()
        );
    }
}
