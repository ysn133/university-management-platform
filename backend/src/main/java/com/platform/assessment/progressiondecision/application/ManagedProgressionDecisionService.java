package com.platform.assessment.progressiondecision.application;

import com.platform.academicregistration.registration.domain.AcademicRegistration;
import com.platform.academicregistration.registration.domain.AcademicRegistrationStatus;
import com.platform.academicregistration.registration.infrastructure.AcademicRegistrationRepository;
import com.platform.assessment.progressiondecision.domain.ProgressionDecision;
import com.platform.assessment.progressiondecision.infrastructure.ProgressionDecisionRepository;
import com.platform.assessment.progressiondecision.presentation.dto.ManagedProgressionDecisionResponse;
import com.platform.assessment.progressiondecision.presentation.dto.AcademicYearModuleResultResponse;
import com.platform.assessment.progressiondecision.presentation.dto.AcademicYearSemesterResultResponse;
import com.platform.assessment.moduleresult.domain.ModuleResult;
import com.platform.assessment.moduleresult.infrastructure.ModuleResultRepository;
import com.platform.assessment.semesterresult.domain.SemesterResult;
import com.platform.assessment.semesterresult.infrastructure.SemesterResultRepository;
import com.platform.academicregistration.semesterregistration.infrastructure.SemesterRegistrationRepository;
import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.UserProfile;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.academiclevelruleassignment.infrastructure.AcademicLevelRuleAssignmentRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ManagedProgressionDecisionService {

    private final AcademicRegistrationRepository registrationRepository;
    private final ProgressionDecisionRepository decisionRepository;
    private final AcademicLevelRuleAssignmentRepository ruleAssignmentRepository;
    private final ProgressionDecisionService progressionDecisionService;
    private final AdminPermissionAuthorizationService authorizationService;
    private final UserProfileRepository profileRepository;
    private final SemesterResultRepository semesterResultRepository;
    private final SemesterRegistrationRepository semesterRegistrationRepository;
    private final ModuleResultRepository moduleResultRepository;

    public ManagedProgressionDecisionService(
        AcademicRegistrationRepository registrationRepository,
        ProgressionDecisionRepository decisionRepository,
        AcademicLevelRuleAssignmentRepository ruleAssignmentRepository,
        ProgressionDecisionService progressionDecisionService,
        AdminPermissionAuthorizationService authorizationService,
        UserProfileRepository profileRepository,
        SemesterResultRepository semesterResultRepository,
        SemesterRegistrationRepository semesterRegistrationRepository,
        ModuleResultRepository moduleResultRepository
    ) {
        this.registrationRepository = registrationRepository;
        this.decisionRepository = decisionRepository;
        this.ruleAssignmentRepository = ruleAssignmentRepository;
        this.progressionDecisionService = progressionDecisionService;
        this.authorizationService = authorizationService;
        this.profileRepository = profileRepository;
        this.semesterResultRepository = semesterResultRepository;
        this.semesterRegistrationRepository = semesterRegistrationRepository;
        this.moduleResultRepository = moduleResultRepository;
    }

    @Transactional(readOnly = true)
    public List<ManagedProgressionDecisionResponse> get(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId,
        UUID academicYearId
    ) {
        List<AcademicRegistration> registrations = registrations(academicLevelId, academicYearId);
        authorize(principal, registrations);
        return responses(registrations);
    }

    @Transactional(readOnly = true)
    public List<ManagedProgressionDecisionResponse> getMine(
        AuthenticatedUserPrincipal principal
    ) {
        if (principal == null || principal.role() != AccountRoleType.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
        }
        return responses(registrationRepository
            .findByStudentIdOrderByAcademicYearStartYearDesc(principal.roleEntityId()));
    }

    @Transactional
    public List<ManagedProgressionDecisionResponse> generate(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId,
        UUID academicYearId
    ) {
        List<AcademicRegistration> registrations = registrations(academicLevelId, academicYearId);
        authorize(principal, registrations);
        var assignment = ruleAssignmentRepository
            .findByAcademicLevelIdAndAcademicYearId(academicLevelId, academicYearId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No academic rule profile is assigned to this level and academic year"
            ));
        if (registrations.stream().anyMatch(registration ->
            registration.getAcademicLevel().isTerminalLevel()
        ) && assignment.getAcademicRuleProfile().isAllowProgressionWithDebt()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Progression cannot be generated for a final academic level while its rule profile allows progression with debt"
            );
        }
        long incompleteRegistrations = registrations.stream()
            .filter(registration -> !hasCompleteSemesterResults(registration))
            .count();
        if (incompleteRegistrations > 0) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Progression cannot be generated because semester results are incomplete for "
                    + incompleteRegistrations + " of " + registrations.size() + " students"
            );
        }
        registrations.forEach(registration -> progressionDecisionService.recalculateIfComplete(
            registration,
            assignment.getAcademicRuleProfile()
        ));
        return responses(registrations);
    }

    private boolean hasCompleteSemesterResults(AcademicRegistration registration) {
        int registrationCount = semesterRegistrationRepository
            .findByAcademicRegistrationId(registration.getId())
            .size();
        int resultCount = semesterResultRepository
            .findBySemesterRegistrationAcademicRegistrationId(registration.getId())
            .size();
        return registrationCount > 0 && registrationCount == resultCount;
    }

    private List<AcademicRegistration> registrations(UUID academicLevelId, UUID academicYearId) {
        return registrationRepository
            .findByAcademicLevelIdAndAcademicYearIdAndStatusOrderByStudentApogeeCodeAsc(
                academicLevelId,
                academicYearId,
                AcademicRegistrationStatus.ACTIVE
            );
    }

    private void authorize(
        AuthenticatedUserPrincipal principal,
        List<AcademicRegistration> registrations
    ) {
        if (registrations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No active academic registrations found");
        }
        authorizationService.requirePermission(
            principal,
            registrations.get(0).getStudent().getEstablishment().getId(),
            PermissionCode.GRADE_VIEW
        );
    }

    private List<ManagedProgressionDecisionResponse> responses(
        List<AcademicRegistration> registrations
    ) {
        Map<UUID, AcademicRegistration> registrationsById = registrations.stream()
            .collect(Collectors.toMap(AcademicRegistration::getId, Function.identity()));
        return decisionRepository.findByAcademicRegistrationIdIn(registrationsById.keySet())
            .stream()
            .map(decision -> response(decision, registrationsById.get(
                decision.getAcademicRegistration().getId()
            )))
            .sorted((left, right) -> left.apogeeCode().compareToIgnoreCase(right.apogeeCode()))
            .toList();
    }

    private ManagedProgressionDecisionResponse response(
        ProgressionDecision decision,
        AcademicRegistration registration
    ) {
        UserProfile profile = profileRepository
            .findByUserAccountId(registration.getStudent().getUserAccount().getId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Student profile not found"
            ));
        List<SemesterResult> semesterResults = semesterResultRepository
            .findBySemesterRegistrationAcademicRegistrationId(registration.getId());
        Map<UUID, List<ModuleResult>> moduleResultsBySemesterRegistration = moduleResultRepository
            .findByModuleRegistrationSemesterRegistrationAcademicRegistrationId(registration.getId())
            .stream()
            .collect(Collectors.groupingBy(result -> result.getModuleRegistration()
                .getSemesterRegistration().getId()));
        List<AcademicYearSemesterResultResponse> semesterResponses = semesterResults.stream()
            .sorted((left, right) -> Integer.compare(
                left.getSemesterRegistration().getSemester().getSemesterOrder(),
                right.getSemesterRegistration().getSemester().getSemesterOrder()
            ))
            .map(result -> semesterResponse(
                result,
                moduleResultsBySemesterRegistration.getOrDefault(
                    result.getSemesterRegistration().getId(),
                    List.of()
                )
            ))
            .toList();
        return new ManagedProgressionDecisionResponse(
            decision.getId(),
            registration.getId(),
            registration.getStudent().getId(),
            profile.getFirstName(),
            profile.getLastName(),
            registration.getStudent().getApogeeCode(),
            registration.getStudent().getNationalStudentCode(),
            profile.getCin(),
            registration.getProgramFiliere().getName(),
            registration.getProgramFiliere().getProgramPath().getName(),
            registration.getAcademicLevel().getName(),
            registration.getAcademicYear().getLabel(),
            semesterResponses,
            decision.getDecisionStatus(),
            decision.getAnnualAverage(),
            decision.getOutstandingModuleCount(),
            decision.getDecidedAt()
        );
    }

    private AcademicYearSemesterResultResponse semesterResponse(
        SemesterResult result,
        List<ModuleResult> moduleResults
    ) {
        var semester = result.getSemesterRegistration().getSemester();
        List<AcademicYearModuleResultResponse> modules = moduleResults.stream()
            .sorted((left, right) -> left.getModuleRegistration().getSubjectModule().getCode()
                .compareToIgnoreCase(right.getModuleRegistration().getSubjectModule().getCode()))
            .map(moduleResult -> new AcademicYearModuleResultResponse(
                moduleResult.getModuleRegistration().getSubjectModule().getId(),
                moduleResult.getModuleRegistration().getSubjectModule().getCode(),
                moduleResult.getModuleRegistration().getSubjectModule().getTitle(),
                moduleResult.getFinalGradeValue(),
                moduleResult.getResultStatus(),
                moduleResult.getModuleRegistration().getInscriptionNumber()
            ))
            .toList();
        return new AcademicYearSemesterResultResponse(
            semester.getId(),
            semester.getName(),
            semester.getSemesterOrder(),
            result.getSemesterAverage(),
            result.getResultStatus(),
            modules
        );
    }
}
