package com.platform.assessment.moduleresult.application;

import com.platform.academicregistration.moduleregistration.domain.ModuleRegistration;
import com.platform.academicregistration.moduleregistration.infrastructure.ModuleRegistrationRepository;
import com.platform.assessment.graderecord.domain.GradeRecord;
import com.platform.assessment.graderecord.domain.GradeWorkflowStatus;
import com.platform.assessment.graderecord.infrastructure.GradeRecordRepository;
import com.platform.assessment.graduationdecision.application.GraduationDecisionService;
import com.platform.assessment.moduleresult.domain.ModuleResult;
import com.platform.assessment.moduleresult.domain.ModuleResultStatus;
import com.platform.assessment.moduleresult.infrastructure.ModuleResultRepository;
import com.platform.assessment.semesterresult.application.SemesterResultService;
import com.platform.scheduling.examschedule.domain.ExamSessionType;
import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfile;
import com.platform.universitygovernance.academicruleprofile.application.AcademicRuleProfileResolver;
import com.platform.universitygovernance.academicruleprofile.domain.SessionGradePolicy;
import com.platform.universitygovernance.academicruleprofile.application.AcademicRuleEvaluator;
import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicMetric;
import com.platform.universitygovernance.academicruleprofile.domain.rules.AcademicRuleOutcome;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ModuleResultService {

    private final ModuleResultRepository moduleResultRepository;
    private final GradeRecordRepository gradeRecordRepository;
    private final AcademicRuleProfileResolver ruleProfileResolver;
    private final SemesterResultService semesterResultService;
    private final AcademicRuleEvaluator ruleEvaluator;
    private final ModuleRegistrationRepository moduleRegistrationRepository;
    private final GraduationDecisionService graduationDecisionService;

    public ModuleResultService(
        ModuleResultRepository moduleResultRepository,
        GradeRecordRepository gradeRecordRepository,
        AcademicRuleProfileResolver ruleProfileResolver,
        SemesterResultService semesterResultService,
        AcademicRuleEvaluator ruleEvaluator,
        ModuleRegistrationRepository moduleRegistrationRepository,
        GraduationDecisionService graduationDecisionService
    ) {
        this.moduleResultRepository = moduleResultRepository;
        this.gradeRecordRepository = gradeRecordRepository;
        this.ruleProfileResolver = ruleProfileResolver;
        this.semesterResultService = semesterResultService;
        this.ruleEvaluator = ruleEvaluator;
        this.moduleRegistrationRepository = moduleRegistrationRepository;
        this.graduationDecisionService = graduationDecisionService;
    }

    @Transactional
    public void recalculateForPublishedRecords(List<GradeRecord> publishedRecords) {
        Map<UUID, ModuleRegistration> registrations = new LinkedHashMap<>();
        publishedRecords.forEach(record -> registrations.put(
            record.getModuleRegistration().getId(),
            record.getModuleRegistration()
        ));
        registrations.values().forEach(this::recalculate);
    }

    public void recalculate(ModuleRegistration moduleRegistration) {
        Map<ExamSessionType, GradeRecord> gradesBySession = publishedGradesBySession(
            moduleRegistration.getId()
        );
        GradeRecord normalGrade = gradesBySession.get(ExamSessionType.NORMAL);
        if (normalGrade == null) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A published Normal grade is required before calculating the module result"
            );
        }

        AcademicRuleProfile ruleProfile = ruleProfileResolver.resolveForSemester(
            moduleRegistration.getSemesterRegistration().getSemester()
        );
        BigDecimal finalValue = resolveFinalValue(
            normalGrade.getGradeValue(),
            gradeValue(gradesBySession.get(ExamSessionType.RATTRAPAGE)),
            ruleProfile
        ).setScale(2, RoundingMode.HALF_UP);

        ModuleResult moduleResult = moduleResultRepository
            .findByModuleRegistrationId(moduleRegistration.getId())
            .orElseGet(ModuleResult::new);
        moduleResult.setModuleRegistration(moduleRegistration);
        moduleResult.setAcademicRuleProfile(ruleProfile);
        moduleResult.setFinalGradeValue(finalValue);
        AcademicRuleOutcome moduleOutcome = ruleEvaluator.evaluate(
            ruleProfile.getRuleDefinition().moduleRules(),
            Map.of(
                AcademicMetric.MODULE_FINAL_GRADE, finalValue,
                AcademicMetric.MODULE_INSCRIPTION_NUMBER,
                    BigDecimal.valueOf(moduleRegistration.getInscriptionNumber())
            ),
            ruleProfile
        ).orElseThrow(() -> new ResponseStatusException(
            HttpStatus.CONFLICT,
            "No module result rule matched the calculated grade"
        ));
        moduleResult.setResultStatus(switch (moduleOutcome) {
            case MODULE_VALIDATED -> ModuleResultStatus.V;
            case MODULE_NON_VALIDATED -> ModuleResultStatus.NV;
            default -> throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The matched module rule returned an invalid outcome"
            );
        });
        moduleResult.setCalculatedAt(Instant.now());
        moduleResultRepository.save(moduleResult);

        semesterResultService.recalculateIfComplete(
            moduleRegistration.getSemesterRegistration()
        );
        propagateLaterInscription(moduleRegistration, moduleResult);
    }

    private void propagateLaterInscription(
        ModuleRegistration registration,
        ModuleResult latestResult
    ) {
        if (registration.getInscriptionNumber() <= 1) {
            return;
        }
        UUID studentId = registration.getSemesterRegistration()
            .getAcademicRegistration().getStudent().getId();
        List<ModuleRegistration> earlier = moduleRegistrationRepository
            .findEarlierInscription(
                studentId,
                registration.getSubjectModule().getCode(),
                registration.getInscriptionNumber()
            );
        if (earlier.isEmpty()) {
            return;
        }

        ModuleRegistration original = earlier.get(earlier.size() - 1);
        ModuleResult effectiveOriginal = moduleResultRepository
            .findByModuleRegistrationId(original.getId())
            .orElseGet(ModuleResult::new);
        if (effectiveOriginal.getId() != null
            && effectiveOriginal.getOriginalFinalGradeValue() == null) {
            effectiveOriginal.setOriginalFinalGradeValue(
                effectiveOriginal.getFinalGradeValue()
            );
            effectiveOriginal.setOriginalResultStatus(
                effectiveOriginal.getResultStatus()
            );
            semesterResultService.preserveOriginalSnapshot(
                original.getSemesterRegistration()
            );
        }
        effectiveOriginal.setModuleRegistration(original);
        effectiveOriginal.setAcademicRuleProfile(latestResult.getAcademicRuleProfile());
        effectiveOriginal.setFinalGradeValue(latestResult.getFinalGradeValue());
        effectiveOriginal.setResultStatus(latestResult.getResultStatus());
        effectiveOriginal.setCalculatedAt(Instant.now());
        moduleResultRepository.save(effectiveOriginal);
        semesterResultService.recalculateIfComplete(original.getSemesterRegistration());
        graduationDecisionService.recalculateExistingAfterHistoricalResultChange(
            original.getSemesterRegistration().getAcademicRegistration()
        );
    }

    private Map<ExamSessionType, GradeRecord> publishedGradesBySession(
        UUID moduleRegistrationId
    ) {
        Map<ExamSessionType, GradeRecord> gradesBySession = new EnumMap<>(
            ExamSessionType.class
        );
        gradeRecordRepository.findByModuleRegistrationIdAndWorkflowStatus(
            moduleRegistrationId,
            GradeWorkflowStatus.PUBLISHED
        ).forEach(record -> {
            ExamSessionType sessionType = record.getModuleExam()
                .getExamSchedule()
                .getSessionType();
            if (gradesBySession.put(sessionType, record) != null) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "More than one published grade exists for the same examination session"
                );
            }
        });
        return gradesBySession;
    }

    private BigDecimal resolveFinalValue(
        BigDecimal normalGrade,
        BigDecimal rattrapageGrade,
        AcademicRuleProfile ruleProfile
    ) {
        if (rattrapageGrade == null) {
            return normalGrade;
        }

        SessionGradePolicy policy = ruleProfile.getSessionGradePolicy();
        return switch (policy) {
            case BEST_GRADE -> normalGrade.max(rattrapageGrade);
            case RATTRAPAGE_REPLACES_NORMAL -> rattrapageGrade;
            case RATTRAPAGE_CAPPED_AT_VALIDATION_THRESHOLD -> normalGrade.max(
                rattrapageGrade.min(ruleProfile.getModuleValidationThreshold())
            );
        };
    }

    private BigDecimal gradeValue(GradeRecord gradeRecord) {
        return gradeRecord == null ? null : gradeRecord.getGradeValue();
    }
}
