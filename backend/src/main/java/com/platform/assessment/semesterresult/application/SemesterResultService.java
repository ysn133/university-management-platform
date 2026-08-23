package com.platform.assessment.semesterresult.application;

import com.platform.academicregistration.semesterregistration.domain.SemesterRegistration;
import com.platform.academicregistration.moduleregistration.domain.ModuleRegistrationStatus;
import com.platform.academicregistration.moduleregistration.infrastructure.ModuleRegistrationRepository;
import com.platform.assessment.moduleresult.domain.ModuleResult;
import com.platform.assessment.moduleresult.domain.ModuleResultStatus;
import com.platform.assessment.moduleresult.infrastructure.ModuleResultRepository;
import com.platform.assessment.progressiondecision.application.ProgressionDecisionService;
import com.platform.assessment.semesterresult.domain.SemesterResult;
import com.platform.assessment.semesterresult.domain.SemesterResultStatus;
import com.platform.assessment.semesterresult.infrastructure.SemesterResultRepository;
import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfile;
import com.platform.universitygovernance.academicruleprofile.application.AcademicRuleProfileResolver;
import com.platform.universitygovernance.academicruleprofile.application.AcademicRuleEvaluator;
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
public class SemesterResultService {

    private final SemesterResultRepository semesterResultRepository;
    private final ModuleResultRepository moduleResultRepository;
    private final ModuleRegistrationRepository moduleRegistrationRepository;
    private final ProgressionDecisionService progressionDecisionService;
    private final AcademicRuleEvaluator ruleEvaluator;
    private final AcademicRuleProfileResolver ruleProfileResolver;

    public SemesterResultService(
        SemesterResultRepository semesterResultRepository,
        ModuleResultRepository moduleResultRepository,
        ModuleRegistrationRepository moduleRegistrationRepository,
        ProgressionDecisionService progressionDecisionService,
        AcademicRuleEvaluator ruleEvaluator,
        AcademicRuleProfileResolver ruleProfileResolver
    ) {
        this.semesterResultRepository = semesterResultRepository;
        this.moduleResultRepository = moduleResultRepository;
        this.moduleRegistrationRepository = moduleRegistrationRepository;
        this.progressionDecisionService = progressionDecisionService;
        this.ruleEvaluator = ruleEvaluator;
        this.ruleProfileResolver = ruleProfileResolver;
    }

    @Transactional
    public void recalculateIfComplete(SemesterRegistration semesterRegistration) {
        AcademicRuleProfile ruleProfile = ruleProfileResolver.resolveForSemester(
            semesterRegistration.getSemester()
        );
        int requiredModuleCount = moduleRegistrationRepository
            .findBySemesterRegistrationIdAndStatus(
                semesterRegistration.getId(),
                ModuleRegistrationStatus.ACTIVE
            )
            .size();
        List<ModuleResult> moduleResults = moduleResultRepository
            .findByModuleRegistrationSemesterRegistrationId(
                semesterRegistration.getId()
            );
        if (requiredModuleCount == 0 || moduleResults.size() != requiredModuleCount) {
            return;
        }

        BigDecimal semesterAverage = average(moduleResults);
        long individuallyValidated = moduleResults.stream().filter(result ->
            result.getFinalGradeValue().compareTo(
                ruleProfile.getModuleValidationThreshold()
            ) >= 0
        ).count();
        List<ModuleResult> nonValidated = moduleResults.stream().filter(result ->
            result.getFinalGradeValue().compareTo(
                ruleProfile.getModuleValidationThreshold()
            ) < 0
        ).toList();
        Map<AcademicMetric, BigDecimal> metrics = new EnumMap<>(AcademicMetric.class);
        metrics.put(AcademicMetric.SEMESTER_AVERAGE, semesterAverage);
        metrics.put(
            AcademicMetric.INDIVIDUALLY_VALIDATED_MODULE_COUNT,
            BigDecimal.valueOf(individuallyValidated)
        );
        metrics.put(
            AcademicMetric.NON_VALIDATED_MODULE_COUNT,
            BigDecimal.valueOf(nonValidated.size())
        );
        metrics.put(
            AcademicMetric.MINIMUM_NON_VALIDATED_MODULE_GRADE,
            minimumGradeOrMaximum(nonValidated)
        );
        AcademicRuleOutcome semesterOutcome = ruleEvaluator.evaluate(
            ruleProfile.getRuleDefinition().semesterRulesFor(
                semesterRegistration.getSemester().getTermType()
            ),
            metrics,
            ruleProfile
        ).orElseThrow(() -> new ResponseStatusException(
            HttpStatus.CONFLICT,
            "No semester result rule matched the calculated results"
        ));
        boolean semesterValidated = semesterOutcome
            == AcademicRuleOutcome.SEMESTER_VALIDATED
            || semesterOutcome == AcademicRuleOutcome.SEMESTER_VALIDATED_BY_COMPENSATION;
        boolean compensationAllowed = semesterValidated && !nonValidated.isEmpty();
        moduleResults.forEach(result -> result.setResultStatus(resolveModuleStatus(
            result.getFinalGradeValue(),
            compensationAllowed,
            ruleProfile
        )));
        moduleResultRepository.saveAll(moduleResults);

        SemesterResult semesterResult = semesterResultRepository
            .findBySemesterRegistrationId(semesterRegistration.getId())
            .orElseGet(SemesterResult::new);
        semesterResult.setSemesterRegistration(semesterRegistration);
        semesterResult.setAcademicRuleProfile(ruleProfile);
        semesterResult.setSemesterAverage(semesterAverage);
        semesterResult.setResultStatus(switch (semesterOutcome) {
            case SEMESTER_VALIDATED, SEMESTER_VALIDATED_BY_COMPENSATION ->
                SemesterResultStatus.VALIDATED;
            case SEMESTER_NON_VALIDATED -> SemesterResultStatus.NON_VALIDATED;
            default -> throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The matched semester rule returned an invalid outcome"
            );
        });
        semesterResult.setEvaluatedAt(Instant.now());
        semesterResultRepository.save(semesterResult);

        progressionDecisionService.recalculateIfComplete(
            semesterRegistration.getAcademicRegistration(),
            ruleProfileResolver.resolveForAcademicLevel(
                semesterRegistration.getAcademicRegistration().getAcademicLevel().getId(),
                semesterRegistration.getAcademicRegistration().getAcademicYear().getId()
            )
        );
    }

    @Transactional
    public void preserveOriginalSnapshot(SemesterRegistration semesterRegistration) {
        semesterResultRepository.findBySemesterRegistrationId(semesterRegistration.getId())
            .filter(result -> result.getOriginalSemesterAverage() == null)
            .ifPresent(result -> {
                result.setOriginalSemesterAverage(result.getSemesterAverage());
                result.setOriginalResultStatus(result.getResultStatus());
                semesterResultRepository.save(result);
            });
    }

    private ModuleResultStatus resolveModuleStatus(
        BigDecimal finalGrade,
        boolean compensationAllowed,
        AcademicRuleProfile ruleProfile
    ) {
        if (finalGrade.compareTo(ruleProfile.getModuleValidationThreshold()) >= 0) {
            return ModuleResultStatus.V;
        }
        if (compensationAllowed && finalGrade.compareTo(
            ruleProfile.getCompensationMinimumThreshold()
        ) >= 0) {
            return ModuleResultStatus.AV;
        }
        return ModuleResultStatus.NV;
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
