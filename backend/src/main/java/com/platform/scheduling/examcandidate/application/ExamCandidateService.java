package com.platform.scheduling.examcandidate.application;

import com.platform.academicregistration.subjectmoduleregestration.domain.SubjectModuleRegestration;
import com.platform.academicregistration.subjectmoduleregestration.domain.SubjectModuleRegistrationStatus;
import com.platform.academicregistration.subjectmoduleregestration.infrastructure.SubjectRegestrationRepository;
import com.platform.assessment.graderecord.infrastructure.GradeRecordRepository;
import com.platform.assessment.graderecord.domain.GradeWorkflowStatus;
import com.platform.attendance.absencerecord.infrastructure.AbsenceRecordRepository;
import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.examcandidate.domain.ExamCandidate;
import com.platform.scheduling.examcandidate.infrastructure.ExamCandidateRepository;
import com.platform.scheduling.examcandidate.presentation.dto.ExamCandidateResponse;
import com.platform.scheduling.examschedule.domain.ExamSessionType;
import com.platform.scheduling.moduleexam.domain.ModuleExam;
import com.platform.scheduling.moduleexam.infrastructure.ModuleExamRepository;
import com.platform.teachingassignment.domain.TeachingAssignment;
import com.platform.universitygovernance.academiclevelruleassignment.domain.AcademicLevelRuleAssignment;
import com.platform.universitygovernance.academiclevelruleassignment.domain.AcademicLevelRuleAssignmentStatus;
import com.platform.universitygovernance.academiclevelruleassignment.infrastructure.AcademicLevelRuleAssignmentRepository;
import com.platform.universitygovernance.academicruleprofile.domain.AbsenceExclusionPolicy;
import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfile;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ExamCandidateService {

    private final ExamCandidateRepository examCandidateRepository;
    private final ModuleExamRepository moduleExamRepository;
    private final SubjectRegestrationRepository moduleRegistrationRepository;
    private final AbsenceRecordRepository absenceRecordRepository;
    private final AcademicLevelRuleAssignmentRepository ruleAssignmentRepository;
    private final GradeRecordRepository gradeRecordRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public ExamCandidateService(
        ExamCandidateRepository examCandidateRepository,
        ModuleExamRepository moduleExamRepository,
        SubjectRegestrationRepository moduleRegistrationRepository,
        AbsenceRecordRepository absenceRecordRepository,
        AcademicLevelRuleAssignmentRepository ruleAssignmentRepository,
        GradeRecordRepository gradeRecordRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.examCandidateRepository = examCandidateRepository;
        this.moduleExamRepository = moduleExamRepository;
        this.moduleRegistrationRepository = moduleRegistrationRepository;
        this.absenceRecordRepository = absenceRecordRepository;
        this.ruleAssignmentRepository = ruleAssignmentRepository;
        this.gradeRecordRepository = gradeRecordRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public List<ExamCandidateResponse> generateCandidates(
        AuthenticatedUserPrincipal principal,
        UUID moduleExamId
    ) {
        ModuleExam moduleExam = findModuleExam(moduleExamId);
        permissionAuthorizationService.requirePermission(
            principal,
            moduleExam.getExamSchedule().getEstablishment().getId(),
            PermissionCode.EXAM_SCHEDULE_UPDATE
        );
        if (gradeRecordRepository.existsByModuleExamId(moduleExamId)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Candidates cannot be regenerated after grade entry has started"
            );
        }

        List<SubjectModuleRegestration> registrations = moduleRegistrationRepository
            .findEligibleForModuleExam(
                moduleExam.getSubjectModule().getId(),
                moduleExam.getClassGroup().getId(),
                moduleExam.getExamSchedule().getAcademicYear().getId(),
                moduleExam.getExamSchedule().getSemester().getId(),
                SubjectModuleRegistrationStatus.ACTIVE
            );
        if (registrations.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No active module registrations were found for this exam"
            );
        }

        List<ExamCandidate> candidates = registrations.stream()
            .filter(registration -> isEligibleCandidate(moduleExam, registration))
            .map(registration -> createCandidate(moduleExam, registration))
            .toList();

        examCandidateRepository.deleteAllInBatch(
            examCandidateRepository.findByModuleExamIdOrderByCreatedAtAsc(moduleExamId)
        );
        List<ExamCandidateResponse> responses = examCandidateRepository
            .saveAll(candidates)
            .stream()
            .map(this::toResponse)
            .toList();
        moduleExam.setCandidateListGeneratedAt(Instant.now());
        moduleExamRepository.save(moduleExam);
        return responses;
    }

    @Transactional(readOnly = true)
    public List<ExamCandidateResponse> getCandidates(
        AuthenticatedUserPrincipal principal,
        UUID moduleExamId
    ) {
        ModuleExam moduleExam = findModuleExam(moduleExamId);
        requireProfessorOrManagementView(principal, moduleExam);
        return examCandidateRepository
            .findByModuleExamIdOrderByCreatedAtAsc(moduleExamId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ExamCandidateResponse> getMyInvitations(
        AuthenticatedUserPrincipal principal
    ) {
        if (principal == null || principal.role() != AccountRoleType.STUDENT) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Student access required"
            );
        }
        return examCandidateRepository
            .findPublishedStudentInvitations(principal.roleEntityId())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private ExamCandidate createCandidate(
        ModuleExam moduleExam,
        SubjectModuleRegestration registration
    ) {
        ExamCandidate candidate = new ExamCandidate();
        candidate.setModuleExam(moduleExam);
        candidate.setModuleRegistration(registration);
        return candidate;
    }

    private boolean isEligibleCandidate(
        ModuleExam moduleExam,
        SubjectModuleRegestration registration
    ) {
        AcademicRuleProfile ruleProfile = findRuleProfile(registration);
        long countedAbsences = absenceRecordRepository
            .countByModuleRegistrationIdAndJustifiedFalse(registration.getId());
        boolean absenceLimitExceeded = countedAbsences
            > ruleProfile.getMaximumUnjustifiedAbsences();

        if (moduleExam.getExamSchedule().getSessionType() == ExamSessionType.NORMAL) {
            return !absenceLimitExceeded;
        }
        if (absenceLimitExceeded) {
            return ruleProfile.getAbsenceExclusionPolicy()
                == AbsenceExclusionPolicy.NORMAL_ONLY;
        }

        var normalGrades = gradeRecordRepository
            .findByModuleRegistrationIdAndWorkflowStatus(
                registration.getId(),
                GradeWorkflowStatus.PUBLISHED
            )
            .stream()
            .filter(grade -> grade.getModuleExam().getExamSchedule().getSessionType()
                == ExamSessionType.NORMAL)
            .toList();
        if (normalGrades.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Publish the Normal grade before generating Rattrapage candidates"
            );
        }
        return normalGrades.stream().noneMatch(grade ->
            grade.getGradeValue().compareTo(
                ruleProfile.getModuleValidationThreshold()
            ) >= 0
        );
    }

    private AcademicRuleProfile findRuleProfile(
        SubjectModuleRegestration registration
    ) {
        UUID academicLevelId = registration.getOriginAcademicLevel() == null
            ? registration.getSemesterRegestration()
                .getAcademicRegistration()
                .getAcademicLevel()
                .getId()
            : registration.getOriginAcademicLevel().getId();
        UUID academicYearId = registration.getSemesterRegestration()
            .getAcademicRegistration()
            .getAcademicYear()
            .getId();
        AcademicLevelRuleAssignment assignment = ruleAssignmentRepository
            .findByAcademicLevelIdAndAcademicYearId(academicLevelId, academicYearId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No academic rule profile is assigned to this academic context"
            ));
        if (assignment.getStatus() != AcademicLevelRuleAssignmentStatus.ACTIVE) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The academic rule assignment is not active"
            );
        }
        return assignment.getAcademicRuleProfile();
    }

    private ModuleExam findModuleExam(UUID moduleExamId) {
        return moduleExamRepository.findById(moduleExamId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Module exam not found"
            ));
    }

    private void requireProfessorOrManagementView(
        AuthenticatedUserPrincipal principal,
        ModuleExam moduleExam
    ) {
        TeachingAssignment assignment = moduleExam.getTeachingAssignment();
        if (principal != null
            && principal.role() == AccountRoleType.PROFESSOR
            && assignment != null
            && principal.roleEntityId().equals(assignment.getProfessor().getId())) {
            return;
        }
        permissionAuthorizationService.requirePermission(
            principal,
            moduleExam.getExamSchedule().getEstablishment().getId(),
            PermissionCode.EXAM_SCHEDULE_VIEW
        );
    }

    private ExamCandidateResponse toResponse(ExamCandidate candidate) {
        SubjectModuleRegestration registration = candidate.getModuleRegistration();
        ModuleExam moduleExam = candidate.getModuleExam();
        return new ExamCandidateResponse(
            candidate.getId(),
            moduleExam.getId(),
            registration.getId(),
            registration.getSemesterRegestration()
                .getAcademicRegistration()
                .getStudent()
                .getId(),
            registration.getSubjectModule().getId(),
            moduleExam.getExamSchedule().getSessionType(),
            moduleExam.getExamDate(),
            moduleExam.getStartTime(),
            moduleExam.getLocation(),
            candidate.getCreatedAt()
        );
    }
}
