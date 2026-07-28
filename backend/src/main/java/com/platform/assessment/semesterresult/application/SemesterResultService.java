package com.platform.assessment.semesterresult.application;

import com.platform.academicregistration.semesterregistration.domain.SemesterRegestration;
import com.platform.academicregistration.subjectmoduleregestration.domain.SubjectModuleRegistrationStatus;
import com.platform.academicregistration.subjectmoduleregestration.infrastructure.SubjectRegestrationRepository;
import com.platform.assessment.moduleresult.domain.ModuleResult;
import com.platform.assessment.moduleresult.domain.ModuleResultStatus;
import com.platform.assessment.moduleresult.infrastructure.ModuleResultRepository;
import com.platform.assessment.progressiondecision.application.ProgressionDecisionService;
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
public class SemesterResultService {

    private final SemesterResultRepository semesterResultRepository;
    private final ModuleResultRepository moduleResultRepository;
    private final SubjectRegestrationRepository moduleRegistrationRepository;
    private final ProgressionDecisionService progressionDecisionService;

    public SemesterResultService(
        SemesterResultRepository semesterResultRepository,
        ModuleResultRepository moduleResultRepository,
        SubjectRegestrationRepository moduleRegistrationRepository,
        ProgressionDecisionService progressionDecisionService
    ) {
        this.semesterResultRepository = semesterResultRepository;
        this.moduleResultRepository = moduleResultRepository;
        this.moduleRegistrationRepository = moduleRegistrationRepository;
        this.progressionDecisionService = progressionDecisionService;
    }

    @Transactional
    public void recalculateIfComplete(
        SemesterRegestration semesterRegistration,
        AcademicRuleProfile ruleProfile
    ) {
        int requiredModuleCount = moduleRegistrationRepository
            .findBySemesterRegestrationIdAndStatus(
                semesterRegistration.getId(),
                SubjectModuleRegistrationStatus.ACTIVE
            )
            .size();
        List<ModuleResult> moduleResults = moduleResultRepository
            .findByModuleRegistrationSemesterRegestrationId(
                semesterRegistration.getId()
            );
        if (requiredModuleCount == 0 || moduleResults.size() != requiredModuleCount) {
            return;
        }

        BigDecimal semesterAverage = average(moduleResults);
        boolean compensationAllowed = semesterAverage.compareTo(
            ruleProfile.getSemesterValidationAverage()
        ) >= 0;
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
        semesterResult.setResultStatus(moduleResults.stream().allMatch(result ->
            result.getResultStatus() != ModuleResultStatus.NV
        ) ? SemesterResultStatus.VALIDATED : SemesterResultStatus.NON_VALIDATED);
        semesterResult.setEvaluatedAt(Instant.now());
        semesterResultRepository.save(semesterResult);

        progressionDecisionService.recalculateIfComplete(
            semesterRegistration.getAcademicRegistration(),
            ruleProfile
        );
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
}
