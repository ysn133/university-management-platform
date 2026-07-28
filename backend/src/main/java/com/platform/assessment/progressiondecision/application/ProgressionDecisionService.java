package com.platform.assessment.progressiondecision.application;

import com.platform.academicregistration.registration.domain.AcademicRegistration;
import com.platform.academicregistration.semesterregistration.domain.SemesterRegestration;
import com.platform.academicregistration.semesterregistration.infrastructer.SemesterRegestrationRepository;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressionDecisionService {

    private final ProgressionDecisionRepository progressionDecisionRepository;
    private final SemesterRegestrationRepository semesterRegistrationRepository;
    private final SemesterResultRepository semesterResultRepository;
    private final ModuleResultRepository moduleResultRepository;

    public ProgressionDecisionService(
        ProgressionDecisionRepository progressionDecisionRepository,
        SemesterRegestrationRepository semesterRegistrationRepository,
        SemesterResultRepository semesterResultRepository,
        ModuleResultRepository moduleResultRepository
    ) {
        this.progressionDecisionRepository = progressionDecisionRepository;
        this.semesterRegistrationRepository = semesterRegistrationRepository;
        this.semesterResultRepository = semesterResultRepository;
        this.moduleResultRepository = moduleResultRepository;
    }

    @Transactional
    public void recalculateIfComplete(
        AcademicRegistration academicRegistration,
        AcademicRuleProfile ruleProfile
    ) {
        List<SemesterRegestration> registrations = semesterRegistrationRepository
            .findByAcademicRegistrationId(academicRegistration.getId());
        List<SemesterResult> semesterResults = semesterResultRepository
            .findBySemesterRegistrationAcademicRegistrationId(
                academicRegistration.getId()
            );
        if (registrations.isEmpty() || semesterResults.size() != registrations.size()) {
            return;
        }

        List<ModuleResult> moduleResults = moduleResultRepository
            .findByModuleRegistrationSemesterRegestrationAcademicRegistrationId(
                academicRegistration.getId()
            );
        if (moduleResults.isEmpty()) {
            return;
        }

        BigDecimal annualAverage = average(moduleResults);
        List<ModuleResult> outstanding = moduleResults.stream()
            .filter(result -> result.getResultStatus() == ModuleResultStatus.NV)
            .toList();
        ProgressionDecisionStatus decisionStatus = resolveDecision(
            semesterResults,
            outstanding,
            annualAverage,
            ruleProfile
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
        List<ModuleResult> outstanding,
        BigDecimal annualAverage,
        AcademicRuleProfile ruleProfile
    ) {
        boolean allSemestersValidated = semesterResults.stream().allMatch(result ->
            result.getResultStatus() == SemesterResultStatus.VALIDATED
        );
        if (allSemestersValidated) {
            return ProgressionDecisionStatus.PROMOTED;
        }

        boolean attemptsExhausted = outstanding.stream().anyMatch(result ->
            result.getModuleRegistration().getInscriptionNumber()
                >= ruleProfile.getMaximumModuleInscriptions()
        );
        if (attemptsExhausted) {
            return ProgressionDecisionStatus.FAILED;
        }

        boolean annualAverageAllowsDebt = ruleProfile.getAnnualValidationAverage() == null
            || annualAverage.compareTo(ruleProfile.getAnnualValidationAverage()) >= 0;
        if (ruleProfile.isAllowProgressionWithDebt()
            && outstanding.size() <= ruleProfile.getMaximumCarriedModules()
            && annualAverageAllowsDebt) {
            return ProgressionDecisionStatus.PROMOTED_WITH_DEBT;
        }
        return ProgressionDecisionStatus.REPEAT;
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
}
