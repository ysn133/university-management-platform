package com.platform.assessment.progressiondecision.application;

import com.platform.academicregistration.registration.domain.AcademicRegistration;
import com.platform.academicregistration.semesterregistration.domain.SemesterRegistration;
import com.platform.academicregistration.semesterregistration.infrastructure.SemesterRegistrationRepository;
import com.platform.assessment.moduleresult.domain.ModuleResult;
import com.platform.assessment.moduleresult.domain.ModuleResultStatus;
import com.platform.assessment.moduleresult.infrastructure.ModuleResultRepository;
import com.platform.assessment.progressiondecision.domain.ProgressionDecision;
import com.platform.assessment.progressiondecision.domain.ProgressionDecisionStatus;
import com.platform.assessment.progressiondecision.infrastructure.ProgressionDecisionRepository;
import com.platform.assessment.semesterresult.domain.SemesterResult;
import com.platform.assessment.semesterresult.domain.SemesterResultStatus;
import com.platform.assessment.semesterresult.infrastructure.SemesterResultRepository;
import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfile;
import com.platform.universitygovernance.academicruleprofile.application.AcademicRuleEvaluator;
import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicDecisionRule;
import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicMetric;
import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicRuleOutcome;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressionDecisionService {

    private final ProgressionDecisionRepository progressionDecisionRepository;
    private final SemesterRegistrationRepository semesterRegistrationRepository;
    private final SemesterResultRepository semesterResultRepository;
    private final ModuleResultRepository moduleResultRepository;
    private final AcademicRuleEvaluator ruleEvaluator;

    public ProgressionDecisionService(
        ProgressionDecisionRepository progressionDecisionRepository,
        SemesterRegistrationRepository semesterRegistrationRepository,
        SemesterResultRepository semesterResultRepository,
        ModuleResultRepository moduleResultRepository,
        AcademicRuleEvaluator ruleEvaluator
    ) {
        this.progressionDecisionRepository = progressionDecisionRepository;
        this.semesterRegistrationRepository = semesterRegistrationRepository;
        this.semesterResultRepository = semesterResultRepository;
        this.moduleResultRepository = moduleResultRepository;
        this.ruleEvaluator = ruleEvaluator;
    }

    @Transactional
    public void recalculateIfComplete(
        AcademicRegistration academicRegistration,
        AcademicRuleProfile ruleProfile
    ) {
        List<SemesterRegistration> registrations = semesterRegistrationRepository
            .findByAcademicRegistrationId(academicRegistration.getId());
        List<SemesterResult> semesterResults = semesterResultRepository
            .findBySemesterRegistrationAcademicRegistrationId(
                academicRegistration.getId()
            );
        if (registrations.isEmpty() || semesterResults.size() != registrations.size()) {
            return;
        }

        List<ModuleResult> moduleResults = moduleResultRepository
            .findByModuleRegistrationSemesterRegistrationAcademicRegistrationId(
                academicRegistration.getId()
            ).stream()
            .filter(result -> result.getModuleRegistration().getOriginAcademicLevel() == null
                || result.getModuleRegistration().getOriginAcademicLevel().getId()
                    .equals(academicRegistration.getAcademicLevel().getId()))
            .toList();
        if (moduleResults.isEmpty()) {
            return;
        }

        BigDecimal annualAverage = average(moduleResults);
        List<ModuleResult> outstanding = moduleResults.stream()
            .filter(result -> result.getResultStatus() == ModuleResultStatus.NV)
            .toList();
        ProgressionDecisionStatus decisionStatus = resolveDecision(
            semesterResults,
            moduleResults,
            outstanding,
            annualAverage,
            ruleProfile,
            academicRegistration.getAcademicLevel().isTerminalLevel()
        );

        ProgressionDecision decision = progressionDecisionRepository
            .findByAcademicRegistrationId(academicRegistration.getId())
            .orElseGet(ProgressionDecision::new);
        decision.setAcademicRegistration(academicRegistration);
        decision.setAcademicRuleProfile(ruleProfile);
        decision.setDecisionStatus(decisionStatus);
        decision.setAnnualAverage(annualAverage);
        decision.setOutstandingModuleCount(outstanding.size());
        decision.setDecidedAt(Instant.now());
        progressionDecisionRepository.save(decision);
    }

    private ProgressionDecisionStatus resolveDecision(
        List<SemesterResult> semesterResults,
        List<ModuleResult> moduleResults,
        List<ModuleResult> outstanding,
        BigDecimal annualAverage,
        AcademicRuleProfile ruleProfile,
        boolean terminalLevel
    ) {
        long individuallyValidated = moduleResults.stream().filter(result ->
            result.getFinalGradeValue().compareTo(
                ruleProfile.getModuleValidationThreshold()
            ) >= 0
        ).count();
        long exhausted = outstanding.stream().filter(result ->
            result.getModuleRegistration().getInscriptionNumber()
                >= ruleProfile.getMaximumModuleInscriptions()
        ).count();
        long nonValidatedSemesters = semesterResults.stream().filter(result ->
            result.getResultStatus() != SemesterResultStatus.VALIDATED
        ).count();

        Map<AcademicMetric, BigDecimal> metrics = new EnumMap<>(AcademicMetric.class);
        metrics.put(AcademicMetric.ANNUAL_AVERAGE, annualAverage);
        metrics.put(
            AcademicMetric.INDIVIDUALLY_VALIDATED_MODULE_COUNT,
            BigDecimal.valueOf(individuallyValidated)
        );
        metrics.put(
            AcademicMetric.MINIMUM_NON_VALIDATED_MODULE_GRADE,
            minimumGradeOrMaximum(outstanding)
        );
        metrics.put(
            AcademicMetric.NON_VALIDATED_SEMESTER_COUNT,
            BigDecimal.valueOf(nonValidatedSemesters)
        );
        metrics.put(
            AcademicMetric.OUTSTANDING_MODULE_COUNT,
            BigDecimal.valueOf(outstanding.size())
        );
        metrics.put(
            AcademicMetric.EXHAUSTED_MODULE_INSCRIPTION_COUNT,
            BigDecimal.valueOf(exhausted)
        );

        List<AcademicDecisionRule> academicLevelRules = ruleProfile
            .getRuleDefinition()
            .academicLevelRules()
            .stream()
            .filter(rule -> ruleProfile.isAllowInterSemesterCompensation()
                || rule.outcome()
                    != AcademicRuleOutcome.ACADEMIC_LEVEL_VALIDATED_BY_COMPENSATION)
            .toList();
        AcademicRuleOutcome academicLevelOutcome = ruleEvaluator.evaluate(
            academicLevelRules,
            metrics,
            ruleProfile
        ).orElseThrow(() -> new ResponseStatusException(
            HttpStatus.CONFLICT,
            "No academic-level validation rule matched the calculated results"
        ));
        boolean academicLevelValidated = academicLevelOutcome
            == AcademicRuleOutcome.ACADEMIC_LEVEL_VALIDATED
            || academicLevelOutcome
                == AcademicRuleOutcome.ACADEMIC_LEVEL_VALIDATED_BY_COMPENSATION;
        metrics.put(
            AcademicMetric.ACADEMIC_LEVEL_VALIDATED,
            academicLevelValidated ? BigDecimal.ONE : BigDecimal.ZERO
        );

        List<AcademicDecisionRule> progressionRules = ruleProfile
            .getRuleDefinition()
            .progressionRules()
            .stream()
            .filter(rule -> ruleProfile.isAllowProgressionWithDebt()
                || rule.outcome() != AcademicRuleOutcome.PROMOTED_WITH_DEBT)
            .toList();
        AcademicRuleOutcome outcome = ruleEvaluator.evaluate(
            progressionRules,
            metrics,
            ruleProfile
        ).orElseThrow(() -> new ResponseStatusException(
            HttpStatus.CONFLICT,
            "No progression rule matched the calculated results"
        ));
        return switch (outcome) {
            case PROMOTED -> terminalLevel
                ? ProgressionDecisionStatus.LEVEL_VALIDATED
                : academicLevelOutcome
                    == AcademicRuleOutcome.ACADEMIC_LEVEL_VALIDATED_BY_COMPENSATION
                        ? ProgressionDecisionStatus.PROMOTED_BY_COMPENSATION
                        : ProgressionDecisionStatus.PROMOTED;
            case PROMOTED_WITH_DEBT -> ProgressionDecisionStatus.PROMOTED_WITH_DEBT;
            case REPEAT -> ProgressionDecisionStatus.REPEAT;
            case FAILED -> ProgressionDecisionStatus.FAILED;
            default -> throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The matched progression rule returned an invalid outcome"
            );
        };
    }

    private BigDecimal average(List<ModuleResult> moduleResults) {
        BigDecimal total = moduleResults.stream()
            .map(ModuleResult::getFinalGradeValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(
            BigDecimal.valueOf(moduleResults.size()),
            2,
            RoundingMode.HALF_UP
        );
    }

    private BigDecimal minimumGradeOrMaximum(List<ModuleResult> results) {
        return results.stream()
            .map(ModuleResult::getFinalGradeValue)
            .min(BigDecimal::compareTo)
            .orElse(new BigDecimal("20.00"));
    }

}
