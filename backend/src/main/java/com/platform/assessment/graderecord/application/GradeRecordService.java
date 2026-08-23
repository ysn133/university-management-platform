package com.platform.assessment.graderecord.application;

import com.platform.academicregistration.moduleregistration.domain.ModuleRegistration;
import com.platform.academicregistration.moduleregistration.infrastructure.ModuleRegistrationRepository;
import com.platform.assessment.moduleresult.domain.ModuleResult;
import com.platform.assessment.moduleresult.infrastructure.ModuleResultRepository;
import com.platform.assessment.graderecord.domain.GradeRecord;
import com.platform.assessment.graderecord.domain.GradeResultView;
import com.platform.assessment.graderecord.domain.GradeWorkflowStatus;
import com.platform.assessment.graderecord.domain.ZeroGradeReason;
import com.platform.assessment.graderecord.infrastructure.GradeRecordRepository;
import com.platform.assessment.graderecord.presentation.dto.GradeItemRequest;
import com.platform.assessment.graderecord.presentation.dto.GradeItemResponse;
import com.platform.assessment.graderecord.presentation.dto.GradeSheetResponse;
import com.platform.assessment.graderecord.presentation.dto.SaveGradeSheetRequest;
import com.platform.assessment.graderecord.presentation.dto.StudentGradeResponse;
import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.Student;
import com.platform.identityaccess.infrastructure.StudentRepository;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.examcandidate.infrastructure.ExamCandidateRepository;
import com.platform.scheduling.examschedule.domain.PublicationStatus;
import com.platform.scheduling.moduleexam.domain.ModuleExam;
import com.platform.scheduling.moduleexam.infrastructure.ModuleExamRepository;
import com.platform.moduleclassresponsibility.domain.ModuleClassResponsibility;
import com.platform.moduleclassresponsibility.domain.ModuleClassResponsibilityStatus;
import com.platform.moduleclassresponsibility.infrastructure.ModuleClassResponsibilityRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GradeRecordService {

    private final GradeRecordRepository gradeRecordRepository;
    private final ModuleExamRepository moduleExamRepository;
    private final StudentRepository studentRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;
    private final ModuleResultRepository moduleResultRepository;
    private final ExamCandidateRepository examCandidateRepository;
    private final ModuleClassResponsibilityRepository responsibilityRepository;
    private final UserProfileRepository userProfileRepository;
    private final ModuleRegistrationRepository moduleRegistrationRepository;

    public GradeRecordService(
        GradeRecordRepository gradeRecordRepository,
        ModuleExamRepository moduleExamRepository,
        StudentRepository studentRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService,
        ModuleResultRepository moduleResultRepository,
        ExamCandidateRepository examCandidateRepository,
        ModuleClassResponsibilityRepository responsibilityRepository,
        UserProfileRepository userProfileRepository,
        ModuleRegistrationRepository moduleRegistrationRepository
    ) {
        this.gradeRecordRepository = gradeRecordRepository;
        this.moduleExamRepository = moduleExamRepository;
        this.studentRepository = studentRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
        this.moduleResultRepository = moduleResultRepository;
        this.examCandidateRepository = examCandidateRepository;
        this.responsibilityRepository = responsibilityRepository;
        this.userProfileRepository = userProfileRepository;
        this.moduleRegistrationRepository = moduleRegistrationRepository;
    }

    @Transactional(readOnly = true)
    public GradeSheetResponse getGradeSheet(
        AuthenticatedUserPrincipal principal,
        UUID moduleExamId
    ) {
        ModuleExam moduleExam = findModuleExam(moduleExamId);
        requireProfessorOrManagementView(principal, moduleExam);
        return buildGradeSheet(moduleExam);
    }

    @Transactional
    public GradeSheetResponse saveDraftGradeSheet(
        AuthenticatedUserPrincipal principal,
        UUID moduleExamId,
        SaveGradeSheetRequest request
    ) {
        ModuleExam moduleExam = findModuleExam(moduleExamId);
        requireAssignedProfessor(principal, moduleExam);
        ensureExamPublished(moduleExam);

        List<ModuleRegistration> eligibleRegistrations = eligibleRegistrations(
            moduleExam
        );
        ensureEligibleStudentsExist(eligibleRegistrations);
        Map<UUID, GradeItemRequest> requestedGrades = indexRequestedGrades(request);
        ensureCompleteGradeSheet(eligibleRegistrations, requestedGrades.keySet());

        List<GradeRecord> existingRecords = gradeRecordRepository
            .findByModuleExamId(moduleExamId);
        ensureStatus(existingRecords, GradeWorkflowStatus.DRAFT);
        ensureNoStaleRecords(existingRecords, requestedGrades.keySet());
        Map<UUID, GradeRecord> recordsByRegistration = existingRecords.stream()
            .collect(Collectors.toMap(
                record -> record.getModuleRegistration().getId(),
                Function.identity()
            ));

        List<GradeRecord> records = eligibleRegistrations.stream()
            .map(registration -> {
                GradeRecord record = recordsByRegistration.getOrDefault(
                    registration.getId(),
                    new GradeRecord()
                );
                record.setModuleRegistration(registration);
                record.setModuleExam(moduleExam);
                record.setGradeValue(
                    requestedGrades.get(registration.getId()).gradeValue()
                );
                record.setZeroGradeReason(
                    requestedGrades.get(registration.getId()).zeroGradeReason()
                );
                record.setWorkflowStatus(GradeWorkflowStatus.DRAFT);
                record.setPublishedAt(null);
                return record;
            })
            .toList();

        gradeRecordRepository.saveAll(records);
        return buildGradeSheet(moduleExam);
    }

    @Transactional
    public GradeSheetResponse submitGradeSheet(
        AuthenticatedUserPrincipal principal,
        UUID moduleExamId
    ) {
        ModuleExam moduleExam = findModuleExam(moduleExamId);
        requireAssignedProfessor(principal, moduleExam);
        List<GradeRecord> records = requireCompleteStoredSheet(moduleExam);
        transition(
            records,
            GradeWorkflowStatus.DRAFT,
            GradeWorkflowStatus.SUBMITTED,
            null
        );
        return buildGradeSheet(moduleExam);
    }

    @Transactional
    public GradeSheetResponse reviewGradeSheet(
        AuthenticatedUserPrincipal principal,
        UUID moduleExamId
    ) {
        return managementTransition(
            principal,
            moduleExamId,
            PermissionCode.GRADE_REVIEW,
            GradeWorkflowStatus.SUBMITTED,
            GradeWorkflowStatus.REVIEWED,
            null
        );
    }

    @Transactional
    public GradeSheetResponse approveGradeSheet(
        AuthenticatedUserPrincipal principal,
        UUID moduleExamId
    ) {
        return managementTransition(
            principal,
            moduleExamId,
            PermissionCode.GRADE_APPROVE,
            GradeWorkflowStatus.REVIEWED,
            GradeWorkflowStatus.APPROVED,
            null
        );
    }

    @Transactional
    public GradeSheetResponse publishGradeSheet(
        AuthenticatedUserPrincipal principal,
        UUID moduleExamId
    ) {
        ModuleExam moduleExam = findModuleExam(moduleExamId);
        requireManagementPermission(principal, moduleExam, PermissionCode.GRADE_PUBLISH);
        List<GradeRecord> records = requireCompleteStoredSheet(moduleExam);
        transition(
            records,
            GradeWorkflowStatus.APPROVED,
            GradeWorkflowStatus.PUBLISHED,
            Instant.now()
        );
        return buildGradeSheet(moduleExam);
    }

    @Transactional(readOnly = true)
    public List<StudentGradeResponse> getMyGrades(
        AuthenticatedUserPrincipal principal,
        UUID academicYearId,
        UUID academicLevelId,
        UUID semesterId
    ) {
        return getMyGrades(
            principal, academicYearId, academicLevelId, semesterId,
            GradeResultView.EFFECTIVE
        );
    }

    @Transactional(readOnly = true)
    public List<StudentGradeResponse> getMyGrades(
        AuthenticatedUserPrincipal principal,
        UUID academicYearId,
        UUID academicLevelId,
        UUID semesterId,
        GradeResultView resultView
    ) {
        requireStudent(principal);
        return findPublishedStudentGrades(
            principal.roleEntityId(),
            academicYearId,
            academicLevelId,
            semesterId,
            resultView
        );
    }

    @Transactional(readOnly = true)
    public List<StudentGradeResponse> getStudentGrades(
        AuthenticatedUserPrincipal principal,
        UUID studentId,
        UUID academicYearId,
        UUID academicLevelId,
        UUID semesterId
    ) {
        return getStudentGrades(
            principal, studentId, academicYearId, academicLevelId, semesterId,
            GradeResultView.EFFECTIVE
        );
    }

    @Transactional(readOnly = true)
    public List<StudentGradeResponse> getStudentGrades(
        AuthenticatedUserPrincipal principal,
        UUID studentId,
        UUID academicYearId,
        UUID academicLevelId,
        UUID semesterId,
        GradeResultView resultView
    ) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Student not found"
            ));
        permissionAuthorizationService.requirePermission(
            principal,
            student.getEstablishment().getId(),
            PermissionCode.GRADE_VIEW
        );
        return findPublishedStudentGrades(
            studentId,
            academicYearId,
            academicLevelId,
            semesterId,
            resultView
        );
    }

    private List<StudentGradeResponse> findPublishedStudentGrades(
        UUID studentId,
        UUID academicYearId,
        UUID academicLevelId,
        UUID semesterId,
        GradeResultView resultView
    ) {
        List<GradeRecord> records = gradeRecordRepository.findStudentGrades(
            studentId,
            GradeWorkflowStatus.PUBLISHED,
            resultView == GradeResultView.ORIGINAL ? academicYearId : null,
            resultView == GradeResultView.ORIGINAL ? academicLevelId : null,
            resultView == GradeResultView.ORIGINAL ? semesterId : null
        );
        if (records.isEmpty()) {
            return List.of();
        }
        Set<UUID> moduleRegistrationIds = records.stream()
            .map(record -> record.getModuleRegistration().getId())
            .collect(Collectors.toSet());
        Map<UUID, ModuleResult> moduleResults = moduleResultRepository
            .findByModuleRegistrationIdIn(moduleRegistrationIds)
            .stream()
            .collect(Collectors.toMap(
                moduleResult -> moduleResult.getModuleRegistration().getId(),
                Function.identity()
            ));
        if (resultView == GradeResultView.ORIGINAL) {
            return records.stream()
                .map(record -> toStudentGradeResponse(
                    record,
                    record.getModuleRegistration(),
                    originalResult(moduleResults.get(record.getModuleRegistration().getId())),
                    false
                ))
                .toList();
        }

        Map<UUID, ModuleRegistration> originalByRegistration = records.stream()
            .map(GradeRecord::getModuleRegistration)
            .distinct()
            .collect(Collectors.toMap(
                ModuleRegistration::getId,
                registration -> originalRegistration(studentId, registration)
            ));
        Map<UUID, ModuleRegistration> effectiveByOriginal = new HashMap<>();
        records.stream().map(GradeRecord::getModuleRegistration).distinct()
            .filter(registration -> moduleResults.containsKey(registration.getId()))
            .forEach(registration -> {
                ModuleRegistration original = originalByRegistration.get(registration.getId());
                effectiveByOriginal.merge(original.getId(), registration, (current, replacement) ->
                    replacement.getInscriptionNumber() > current.getInscriptionNumber()
                        ? replacement : current
                );
            });

        List<StudentGradeResponse> responses = new ArrayList<>();
        records.stream()
            .filter(record -> {
                ModuleRegistration source = record.getModuleRegistration();
                ModuleRegistration original = originalByRegistration.get(source.getId());
                ModuleRegistration effective = effectiveByOriginal.get(original.getId());
                return effective != null && effective.getId().equals(source.getId());
            })
            .forEach(record -> {
                ModuleRegistration source = record.getModuleRegistration();
                ModuleRegistration original = originalByRegistration.get(source.getId());
                ModuleResult result = moduleResults.get(source.getId());
                boolean revised = !source.getId().equals(original.getId());
                if (matchesGradeContext(original, academicYearId, academicLevelId, semesterId)) {
                    responses.add(toStudentGradeResponse(
                        record, original, result, revised
                    ));
                }
                if (revised && matchesGradeContext(
                    source, academicYearId, academicLevelId, semesterId
                )) {
                    responses.add(toStudentGradeResponse(
                        record, source, result, false
                    ));
                }
            });
        return responses;
    }

    private boolean matchesGradeContext(
        ModuleRegistration registration,
        UUID academicYearId,
        UUID academicLevelId,
        UUID semesterId
    ) {
        var semesterRegistration = registration.getSemesterRegistration();
        var academicRegistration = semesterRegistration.getAcademicRegistration();
        return (academicYearId == null
            || academicYearId.equals(academicRegistration.getAcademicYear().getId()))
            && (academicLevelId == null
                || academicLevelId.equals(academicRegistration.getAcademicLevel().getId()))
            && (semesterId == null
                || semesterId.equals(semesterRegistration.getSemester().getId()));
    }

    private ModuleRegistration originalRegistration(
        UUID studentId,
        ModuleRegistration registration
    ) {
        if (registration.getInscriptionNumber() <= 1) {
            return registration;
        }
        List<ModuleRegistration> earlier = moduleRegistrationRepository.findEarlierInscription(
            studentId,
            registration.getSubjectModule().getCode(),
            registration.getInscriptionNumber()
        );
        return earlier.isEmpty() ? registration : earlier.get(earlier.size() - 1);
    }

    private ModuleResult originalResult(ModuleResult result) {
        if (result == null || result.getOriginalFinalGradeValue() == null) {
            return result;
        }
        ModuleResult original = new ModuleResult();
        original.setModuleRegistration(result.getModuleRegistration());
        original.setAcademicRuleProfile(result.getAcademicRuleProfile());
        original.setFinalGradeValue(result.getOriginalFinalGradeValue());
        original.setResultStatus(result.getOriginalResultStatus());
        original.setCalculatedAt(result.getCalculatedAt());
        return original;
    }

    private GradeSheetResponse managementTransition(
        AuthenticatedUserPrincipal principal,
        UUID moduleExamId,
        PermissionCode permissionCode,
        GradeWorkflowStatus expectedStatus,
        GradeWorkflowStatus targetStatus,
        Instant publishedAt
    ) {
        ModuleExam moduleExam = findModuleExam(moduleExamId);
        requireManagementPermission(principal, moduleExam, permissionCode);
        List<GradeRecord> records = requireCompleteStoredSheet(moduleExam);
        transition(records, expectedStatus, targetStatus, publishedAt);
        return buildGradeSheet(moduleExam);
    }

    private void transition(
        List<GradeRecord> records,
        GradeWorkflowStatus expectedStatus,
        GradeWorkflowStatus targetStatus,
        Instant publishedAt
    ) {
        ensureStatus(records, expectedStatus);
        records.forEach(record -> {
            record.setWorkflowStatus(targetStatus);
            record.setPublishedAt(publishedAt);
        });
        gradeRecordRepository.saveAll(records);
    }

    private List<GradeRecord> requireCompleteStoredSheet(ModuleExam moduleExam) {
        List<ModuleRegistration> eligible = eligibleRegistrations(moduleExam);
        ensureEligibleStudentsExist(eligible);
        List<GradeRecord> records = gradeRecordRepository.findByModuleExamId(
            moduleExam.getId()
        );
        Set<UUID> eligibleIds = eligible.stream()
            .map(ModuleRegistration::getId)
            .collect(Collectors.toSet());
        Set<UUID> storedIds = records.stream()
            .map(record -> record.getModuleRegistration().getId())
            .collect(Collectors.toSet());
        if (!eligibleIds.equals(storedIds)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The grade sheet must contain every eligible module registration"
            );
        }
        return records;
    }

    private Map<UUID, GradeItemRequest> indexRequestedGrades(
        SaveGradeSheetRequest request
    ) {
        Map<UUID, GradeItemRequest> indexed = new HashMap<>();
        request.grades().forEach(item -> {
            ensureValidGrade(item);
            if (indexed.put(item.moduleRegistrationId(), item) != null) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A module registration appears more than once in the grade sheet"
                );
            }
        });
        return indexed;
    }

    private void ensureValidGrade(GradeItemRequest item) {
        if (item.gradeValue() == null
            || item.gradeValue().signum() < 0
            || item.gradeValue().compareTo(new java.math.BigDecimal("20.00")) > 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Grade value must be between 0.00 and 20.00"
            );
        }

        boolean zero = item.gradeValue().signum() == 0;
        ZeroGradeReason reason = item.zeroGradeReason();
        if ((zero && reason == null) || (!zero && reason != null)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A zero-grade reason is required only when the grade value is 0.00"
            );
        }
    }

    private void ensureCompleteGradeSheet(
        List<ModuleRegistration> eligibleRegistrations,
        Set<UUID> requestedRegistrationIds
    ) {
        Set<UUID> eligibleIds = eligibleRegistrations.stream()
            .map(ModuleRegistration::getId)
            .collect(Collectors.toSet());
        if (!eligibleIds.equals(requestedRegistrationIds)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "The grade sheet must contain exactly the eligible module registrations"
            );
        }
    }

    private void ensureNoStaleRecords(
        List<GradeRecord> existingRecords,
        Set<UUID> eligibleRegistrationIds
    ) {
        boolean stale = existingRecords.stream().anyMatch(record ->
            !eligibleRegistrationIds.contains(record.getModuleRegistration().getId())
        );
        if (stale) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Stored grades no longer match the eligible class group"
            );
        }
    }

    private void ensureStatus(
        List<GradeRecord> records,
        GradeWorkflowStatus expectedStatus
    ) {
        boolean invalid = records.stream().anyMatch(record ->
            record.getWorkflowStatus() != expectedStatus
        );
        if (invalid) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Grade sheet must be " + expectedStatus.name()
            );
        }
    }

    private void ensureEligibleStudentsExist(
        List<ModuleRegistration> eligibleRegistrations
    ) {
        if (eligibleRegistrations.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No eligible module registrations were found for this exam"
            );
        }
    }

    private List<ModuleRegistration> eligibleRegistrations(
        ModuleExam moduleExam
    ) {
        if (moduleExam.getCandidateListGeneratedAt() == null) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Generate the exam candidate list before managing grades"
            );
        }
        return examCandidateRepository
            .findByModuleExamIdOrderByCreatedAtAsc(moduleExam.getId())
            .stream()
            .map(candidate -> candidate.getModuleRegistration())
            .toList();
    }

    private GradeSheetResponse buildGradeSheet(ModuleExam moduleExam) {
        List<ModuleRegistration> eligible = eligibleRegistrations(moduleExam);
        Map<UUID, GradeRecord> records = gradeRecordRepository
            .findByModuleExamId(moduleExam.getId())
            .stream()
            .collect(Collectors.toMap(
                record -> record.getModuleRegistration().getId(),
                Function.identity()
            ));

        List<GradeItemResponse> items = eligible.stream()
            .sorted(Comparator.comparing(registration -> registration
                .getSemesterRegistration()
                .getAcademicRegistration()
                .getStudent()
                .getId()))
            .map(registration -> toGradeItemResponse(
                registration,
                records.get(registration.getId())
            ))
            .toList();

        Set<GradeWorkflowStatus> statuses = records.values().stream()
            .map(GradeRecord::getWorkflowStatus)
            .collect(Collectors.toCollection(HashSet::new));
        if (statuses.size() > 1) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Grade records have inconsistent workflow statuses"
            );
        }

        return new GradeSheetResponse(
            moduleExam.getId(),
            moduleExam.getSubjectModule().getId(),
            moduleExam.getClassGroup().getId(),
            statuses.stream().findFirst().orElse(GradeWorkflowStatus.DRAFT),
            items
        );
    }

    private GradeItemResponse toGradeItemResponse(
        ModuleRegistration registration,
        GradeRecord record
    ) {
        Student student = registration.getSemesterRegistration()
            .getAcademicRegistration()
            .getStudent();
        var profile = userProfileRepository.findByUserAccountId(
            student.getUserAccount().getId()
        ).orElseThrow(() -> new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Student profile not found"
        ));
        return new GradeItemResponse(
            record == null ? null : record.getId(),
            registration.getId(),
            student.getId(),
            student.getApogeeCode(),
            student.getUserAccount().getUniversityEmail(),
            profile.getFirstName(),
            profile.getLastName(),
            registration.getInscriptionNumber(),
            record == null ? null : record.getGradeValue(),
            record == null ? null : record.getZeroGradeReason(),
            record == null ? GradeWorkflowStatus.DRAFT : record.getWorkflowStatus(),
            record == null ? null : record.getPublishedAt()
        );
    }

    private StudentGradeResponse toStudentGradeResponse(
        GradeRecord record,
        ModuleRegistration contextRegistration,
        ModuleResult moduleResult,
        boolean revised
    ) {
        ModuleRegistration sourceRegistration = record.getModuleRegistration();
        return new StudentGradeResponse(
            record.getId(),
            contextRegistration.getId(),
            record.getModuleExam().getId(),
            contextRegistration.getSubjectModule().getId(),
            contextRegistration.getSubjectModule().getCode(),
            contextRegistration.getSubjectModule().getTitle(),
            contextRegistration.getSemesterRegistration()
                .getAcademicRegistration()
                .getAcademicYear()
                .getId(),
            contextRegistration.getSemesterRegistration().getSemester().getId(),
            record.getModuleExam().getExamSchedule().getSessionType(),
            sourceRegistration.getInscriptionNumber(),
            record.getGradeValue(),
            record.getZeroGradeReason(),
            record.getPublishedAt(),
            moduleResult == null ? null : moduleResult.getId(),
            moduleResult == null ? null : moduleResult.getFinalGradeValue(),
            moduleResult == null ? null : moduleResult.getResultStatus(),
            moduleResult == null ? null : moduleResult.getAcademicRuleProfile().getId(),
            moduleResult == null ? null : moduleResult.getCalculatedAt(),
            revised,
            sourceRegistration.getSemesterRegistration().getAcademicRegistration()
                .getAcademicYear().getId(),
            sourceRegistration.getSemesterRegistration().getSemester().getId()
        );
    }

    private ModuleExam findModuleExam(UUID moduleExamId) {
        return moduleExamRepository.findById(moduleExamId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Module exam not found"
            ));
    }

    private ModuleClassResponsibility requireAssignedProfessor(
        AuthenticatedUserPrincipal principal,
        ModuleExam moduleExam
    ) {
        ModuleClassResponsibility responsibility = findActiveResponsibility(moduleExam);
        boolean assigned = principal != null
            && principal.role() == AccountRoleType.PROFESSOR
            && responsibility != null
            && responsibility.getStatus() == ModuleClassResponsibilityStatus.ACTIVE
            && principal.roleEntityId().equals(responsibility.getProfessor().getId());
        if (!assigned) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Assigned professor access required"
            );
        }
        return responsibility;
    }

    private void requireProfessorOrManagementView(
        AuthenticatedUserPrincipal principal,
        ModuleExam moduleExam
    ) {
        ModuleClassResponsibility responsibility = findActiveResponsibility(moduleExam);
        if (principal != null
            && principal.role() == AccountRoleType.PROFESSOR
            && responsibility != null
            && principal.roleEntityId().equals(responsibility.getProfessor().getId())) {
            return;
        }
        requireManagementPermission(
            principal,
            moduleExam,
            PermissionCode.GRADE_VIEW
        );
    }

    private ModuleClassResponsibility findActiveResponsibility(ModuleExam moduleExam) {
        return responsibilityRepository
            .findBySubjectModuleIdAndClassGroupIdAndAcademicYearIdAndSemesterIdAndStatus(
                moduleExam.getSubjectModule().getId(),
                moduleExam.getClassGroup().getId(),
                moduleExam.getExamSchedule().getAcademicYear().getId(),
                moduleExam.getExamSchedule().getSemester().getId(),
                ModuleClassResponsibilityStatus.ACTIVE
            )
            .orElse(null);
    }

    private void requireManagementPermission(
        AuthenticatedUserPrincipal principal,
        ModuleExam moduleExam,
        PermissionCode permissionCode
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            moduleExam.getExamSchedule().getEstablishment().getId(),
            permissionCode
        );
    }

    private void requireStudent(AuthenticatedUserPrincipal principal) {
        if (principal == null || principal.role() != AccountRoleType.STUDENT) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Student access required"
            );
        }
    }

    private void ensureExamPublished(ModuleExam moduleExam) {
        if (moduleExam.getExamSchedule().getPublicationStatus()
            != PublicationStatus.PUBLISHED) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Grades can be entered only for a published exam schedule"
            );
        }
    }
}
