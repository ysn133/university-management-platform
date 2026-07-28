package com.platform.assessment.moduleresult.application;

import com.platform.academicregistration.registration.domain.AcademicRegistration;
import com.platform.academicregistration.subjectmoduleregestration.domain.SubjectModuleRegestration;
import com.platform.assessment.graderecord.domain.GradeRecord;
import com.platform.assessment.graderecord.domain.GradeWorkflowStatus;
import com.platform.assessment.graderecord.infrastructure.GradeRecordRepository;
import com.platform.assessment.moduleresult.domain.ModuleResult;
import com.platform.assessment.moduleresult.domain.ModuleResultStatus;
import com.platform.assessment.moduleresult.infrastructure.ModuleResultRepository;
import com.platform.assessment.semesterresult.application.SemesterResultService;
import com.platform.scheduling.examschedule.domain.ExamSessionType;
import com.platform.universitygovernance.academiclevelruleassignment.domain.AcademicLevelRuleAssignment;
import com.platform.universitygovernance.academiclevelruleassignment.infrastructure.AcademicLevelRuleAssignmentRepository;
import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfile;
import com.platform.universitygovernance.academicruleprofile.domain.SessionGradePolicy;
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
    private final AcademicLevelRuleAssignmentRepository ruleAssignmentRepository;
    private final SemesterResultService semesterResultService;

    public ModuleResultService(
        ModuleResultRepository moduleResultRepository,
        GradeRecordRepository gradeRecordRepository,
        AcademicLevelRuleAssignmentRepository ruleAssignmentRepository,
        SemesterResultService semesterResultService
    ) {
        this.moduleResultRepository = moduleResultRepository;
        this.gradeRecordRepository = gradeRecordRepository;
        this.ruleAssignmentRepository = ruleAssignmentRepository;
        this.semesterResultService = semesterResultService;
    }

    @Transactional
    public void recalculateForPublishedRecords(List<GradeRecord> publishedRecords) {
        Map<UUID, SubjectModuleRegestration> registrations = new LinkedHashMap<>();
        publishedRecords.forEach(record -> registrations.put(
            record.getModuleRegistration().getId(),
            record.getModuleRegistration()
        ));
        registrations.values().forEach(this::recalculate);
    }

    private void recalculate(SubjectModuleRegestration moduleRegistration) {
        AcademicRegistration academicRegistration = moduleRegistration
            .getSemesterRegestration()
            .getAcademicRegistration();
        AcademicLevelRuleAssignment assignment = ruleAssignmentRepository
            .findByAcademicLevelIdAndAcademicYearId(
                academicRegistration.getAcademicLevel().getId(),
                academicRegistration.getAcademicYear().getId()
            )
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No academic rule profile is assigned to this level and academic year"
            ));

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

        AcademicRuleProfile ruleProfile = assignment.getAcademicRuleProfile();
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
        moduleResult.setResultStatus(resolveDirectStatus(
            finalValue,
            ruleProfile.getModuleValidationThreshold()
        ));
        moduleResult.setCalculatedAt(Instant.now());
        moduleResultRepository.save(moduleResult);

        semesterResultService.recalculateIfComplete(
            moduleRegistration.getSemesterRegestration(),
            ruleProfile
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

    private ModuleResultStatus resolveDirectStatus(
        BigDecimal finalValue,
        BigDecimal validationThreshold
    ) {
        return finalValue.compareTo(validationThreshold) >= 0
            ? ModuleResultStatus.V
            : ModuleResultStatus.NV;
    }

    private BigDecimal gradeValue(GradeRecord gradeRecord) {
        return gradeRecord == null ? null : gradeRecord.getGradeValue();
    }
}
