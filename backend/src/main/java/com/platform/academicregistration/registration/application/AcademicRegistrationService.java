package com.platform.academicregistration.registration.application;

import com.platform.academicregistration.registration.domain.AcademicRegistration;
import com.platform.academicregistration.registration.domain.AcademicRegistrationStatus;
import com.platform.academicregistration.registration.infrastructure.AcademicRegistrationRepository;
import com.platform.academicregistration.registration.presentation.dto.AcademicRegistrationResponse;
import com.platform.academicregistration.registration.presentation.dto.CreateAcademicRegistrationRequest;
import com.platform.academicregistration.registration.presentation.dto.UpdateAcademicRegistrationRequest;
import com.platform.academicregistration.semesterregistration.application.SemesterRegistrationService;
import com.platform.academicregistration.semesterregistration.domain.SemesterRegistration;
import com.platform.academicregistration.semesterregistration.infrastructure.SemesterRegistrationRepository;
import com.platform.academicregistration.semesterregistration.presentation.dto.SemesterRegistrationResponse;
import com.platform.academicregistration.moduleregistration.application.ModuleRegistrationService;
import com.platform.academicregistration.moduleregistration.presentation.dto.ModuleRegistrationResponse;
import com.platform.academicregistration.classassignment.infrastructure.StudentClassAssignmentRepository;
import com.platform.assessment.progressiondecision.domain.ProgressionDecision;
import com.platform.assessment.progressiondecision.infrastructure.ProgressionDecisionRepository;
import com.platform.assessment.progressiondecision.presentation.dto.ProgressionDecisionResponse;
import com.platform.assessment.semesterresult.domain.SemesterResult;
import com.platform.assessment.semesterresult.infrastructure.SemesterResultRepository;
import com.platform.assessment.semesterresult.presentation.dto.SemesterResultResponse;
import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.Student;
import com.platform.identityaccess.infrastructure.StudentRepository;
import com.platform.identityaccess.domain.UserProfile;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academiclevel.infrastructure.AcademicLevelRepository;
import com.platform.universitygovernance.academiclevelruleassignment.domain.AcademicLevelRuleAssignmentStatus;
import com.platform.universitygovernance.academiclevelruleassignment.infrastructure.AcademicLevelRuleAssignmentRepository;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.universitygovernance.programfiliere.domain.ProgramFiliere;
import com.platform.universitygovernance.programfiliere.infrastructure.ProgramFiliereRepository;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AcademicRegistrationService {

    private final AcademicRegistrationRepository academicRegistrationRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    private final StudentRepository studentRepository;
    private final ProgramFiliereRepository programFiliereRepository;
    private final AcademicLevelRepository academicLevelRepository;
    private final AcademicYearRepository academicYearRepository;
    private final EstablishmentRepository establishmentRepository;
    private final SemesterRegistrationService semesterRegistrationService;
    private final AcademicLevelRuleAssignmentRepository ruleAssignmentRepository;
    private final SemesterRegistrationRepository semesterRegistrationRepository;
    private final ModuleRegistrationService moduleRegistrationService;
    private final UserProfileRepository userProfileRepository;
    private final StudentClassAssignmentRepository studentClassAssignmentRepository;
    private final SemesterResultRepository semesterResultRepository;
    private final ProgressionDecisionRepository progressionDecisionRepository;

    public AcademicRegistrationService(
        AcademicRegistrationRepository academicRegistrationRepository,
        StudentRepository studentRepository,
        ProgramFiliereRepository programFiliereRepository,
        AcademicLevelRepository academicLevelRepository,
        AcademicYearRepository academicYearRepository,
        EstablishmentRepository establishmentRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService,
        SemesterRegistrationService semesterRegistrationService,
        AcademicLevelRuleAssignmentRepository ruleAssignmentRepository,
        SemesterRegistrationRepository semesterRegistrationRepository,
        ModuleRegistrationService moduleRegistrationService,
        UserProfileRepository userProfileRepository,
        StudentClassAssignmentRepository studentClassAssignmentRepository,
        SemesterResultRepository semesterResultRepository,
        ProgressionDecisionRepository progressionDecisionRepository

    ) {
        this.academicRegistrationRepository = academicRegistrationRepository;
        this.studentRepository = studentRepository;
        this.programFiliereRepository = programFiliereRepository;
        this.academicLevelRepository = academicLevelRepository;
        this.academicYearRepository = academicYearRepository;
        this.establishmentRepository = establishmentRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
        this.semesterRegistrationService = semesterRegistrationService;
        this.ruleAssignmentRepository = ruleAssignmentRepository;
        this.semesterRegistrationRepository = semesterRegistrationRepository;
        this.moduleRegistrationService = moduleRegistrationService;
        this.userProfileRepository = userProfileRepository;
        this.studentClassAssignmentRepository = studentClassAssignmentRepository;
        this.semesterResultRepository = semesterResultRepository;
        this.progressionDecisionRepository = progressionDecisionRepository;

    }

    @Transactional
    public AcademicRegistrationResponse createAcademicRegistration(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        CreateAcademicRegistrationRequest request
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.ACADEMIC_REGISTRATION_CREATE
        );

        Establishment establishment = findEstablishment(establishmentId);
        Student student = findStudent(request.studentId());
        ProgramFiliere programFiliere = findProgramFiliere(request.programFiliereId());
        AcademicLevel academicLevel = findAcademicLevel(request.academicLevelId());
        AcademicYear academicYear = findAcademicYear(request.academicYearId());

        ensureRegistrationContext(
            establishment,
            student,
            programFiliere,
            academicLevel,
            academicYear
        );
        ensureActiveRuleAssignment(academicLevel.getId(), academicYear.getId());
        ensureStudentNotRegistered(student.getId(), academicYear.getId());

        AcademicRegistration academicRegistration = new AcademicRegistration();
        academicRegistration.setStudent(student);
        academicRegistration.setProgramFiliere(programFiliere);
        academicRegistration.setAcademicLevel(academicLevel);
        academicRegistration.setAcademicYear(academicYear);
        academicRegistration.setStatus(AcademicRegistrationStatus.ACTIVE);
        AcademicRegistration savedRegistration=academicRegistrationRepository.save(academicRegistration);

        semesterRegistrationService.createSemesterRegistration(savedRegistration);


        return toResponse(savedRegistration);
    }

    @Transactional(readOnly = true)
    public List<AcademicRegistrationResponse> getAcademicRegistrations(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        return getAcademicRegistrations(
            principal,
            establishmentId,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    @Transactional(readOnly = true)
    public List<AcademicRegistrationResponse> getAcademicRegistrations(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        UUID academicYearId,
        UUID programFiliereId,
        UUID academicLevelId,
        UUID semesterId,
        UUID classGroupId,
        AcademicRegistrationStatus status,
        String query
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.ACADEMIC_REGISTRATION_VIEW
        );
        findEstablishment(establishmentId);

        return academicRegistrationRepository
            .findByStudentEstablishmentIdOrderByAcademicYearStartYearDesc(establishmentId)
            .stream()
            .filter(registration -> academicYearId == null
                || academicYearId.equals(registration.getAcademicYear().getId()))
            .filter(registration -> programFiliereId == null
                || programFiliereId.equals(registration.getProgramFiliere().getId()))
            .filter(registration -> academicLevelId == null
                || academicLevelId.equals(registration.getAcademicLevel().getId()))
            .filter(registration -> status == null || status == registration.getStatus())
            .filter(registration -> matchesAcademicPlacement(
                registration,
                semesterId,
                classGroupId
            ))
            .filter(registration -> matchesStudent(registration.getStudent(), query))
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<SemesterRegistrationResponse> getSemesterRegistrations(
        AuthenticatedUserPrincipal principal,
        UUID academicRegistrationId
    ) {
        AcademicRegistration registration = findAcademicRegistration(academicRegistrationId);
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId(registration),
            PermissionCode.ACADEMIC_REGISTRATION_VIEW
        );
        return semesterRegistrationService.getByAcademicRegistration(academicRegistrationId);
    }

    @Transactional(readOnly = true)
    public List<ModuleRegistrationResponse> getModuleRegistrations(
        AuthenticatedUserPrincipal principal,
        UUID semesterRegistrationId
    ) {
        SemesterRegistration semesterRegistration = semesterRegistrationRepository
            .findById(semesterRegistrationId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Semester registration not found"
            ));
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId(semesterRegistration.getAcademicRegistration()),
            PermissionCode.ACADEMIC_REGISTRATION_VIEW
        );
        return moduleRegistrationService.getBySemesterRegistration(semesterRegistrationId);
    }

    @Transactional(readOnly = true)
    public SemesterResultResponse getSemesterResult(
        AuthenticatedUserPrincipal principal,
        UUID semesterRegistrationId
    ) {
        SemesterRegistration registration = findSemesterRegistration(semesterRegistrationId);
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId(registration.getAcademicRegistration()),
            PermissionCode.ACADEMIC_REGISTRATION_VIEW
        );
        SemesterResult result = semesterResultRepository
            .findBySemesterRegistrationId(semesterRegistrationId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Semester result is not available"
            ));
        return new SemesterResultResponse(
            result.getId(),
            semesterRegistrationId,
            result.getAcademicRuleProfile().getId(),
            result.getSemesterAverage(),
            result.getResultStatus(),
            result.getEvaluatedAt()
        );
    }

    @Transactional(readOnly = true)
    public ProgressionDecisionResponse getProgressionDecision(
        AuthenticatedUserPrincipal principal,
        UUID academicRegistrationId
    ) {
        AcademicRegistration registration = findAcademicRegistration(academicRegistrationId);
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId(registration),
            PermissionCode.ACADEMIC_REGISTRATION_VIEW
        );
        ProgressionDecision decision = progressionDecisionRepository
            .findByAcademicRegistrationId(academicRegistrationId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Progression decision is not available"
            ));
        return new ProgressionDecisionResponse(
            decision.getId(),
            academicRegistrationId,
            decision.getAcademicRuleProfile().getId(),
            decision.getDecisionStatus(),
            decision.getAnnualAverage(),
            decision.getOutstandingModuleCount(),
            decision.getDecidedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<AcademicRegistrationResponse> getStudentAcademicRegistrations(
        AuthenticatedUserPrincipal principal,
        UUID studentId
    ) {
        Student student = findStudent(studentId);
        permissionAuthorizationService.requirePermission(
            principal,
            student.getEstablishment().getId(),
            PermissionCode.ACADEMIC_REGISTRATION_VIEW
        );

        return academicRegistrationRepository
            .findByStudentIdOrderByAcademicYearStartYearDesc(studentId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public AcademicRegistrationResponse getAcademicRegistration(
        AuthenticatedUserPrincipal principal,
        UUID academicRegistrationId
    ) {
        AcademicRegistration academicRegistration = findAcademicRegistration(
            academicRegistrationId
        );
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId(academicRegistration),
            PermissionCode.ACADEMIC_REGISTRATION_VIEW
        );
        return toResponse(academicRegistration);
    }

    @Transactional
    public AcademicRegistrationResponse updateAcademicRegistration(
        AuthenticatedUserPrincipal principal,
        UUID academicRegistrationId,
        UpdateAcademicRegistrationRequest request
    ) {
        AcademicRegistration academicRegistration = findAcademicRegistration(
            academicRegistrationId
        );
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId(academicRegistration),
            PermissionCode.ACADEMIC_REGISTRATION_UPDATE
        );

        academicRegistration.setStatus(request.status());
        return toResponse(academicRegistrationRepository.save(academicRegistration));
    }

    private Establishment findEstablishment(UUID establishmentId) {
        return establishmentRepository.findById(establishmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Establishment not found"
            ));
    }

    private ProgramFiliere findProgramFiliere(UUID programFiliereId) {
        return programFiliereRepository.findById(programFiliereId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Program/filiere not found"
            ));
    }

    private AcademicLevel findAcademicLevel(UUID academicLevelId) {
        return academicLevelRepository.findById(academicLevelId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic level not found"
            ));
    }

    private Student findStudent(UUID studentId) {
        return studentRepository.findById(studentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Student not found"
            ));
    }

    private AcademicYear findAcademicYear(UUID academicYearId) {
        return academicYearRepository.findById(academicYearId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic year not found"
            ));
    }

    private AcademicRegistration findAcademicRegistration(UUID academicRegistrationId) {
        return academicRegistrationRepository.findById(academicRegistrationId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic registration not found"
            ));
    }

    private SemesterRegistration findSemesterRegistration(UUID semesterRegistrationId) {
        return semesterRegistrationRepository.findById(semesterRegistrationId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Semester registration not found"
            ));
    }

    private UUID establishmentId(AcademicRegistration academicRegistration) {
        return academicRegistration.getStudent().getEstablishment().getId();
    }

    private boolean matchesStudent(Student student, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String normalized = query.trim().toLowerCase();
        UserProfile profile = userProfileRepository
            .findByUserAccountId(student.getUserAccount().getId())
            .orElse(null);
        return contains(student.getApogeeCode(), normalized)
            || contains(student.getNationalStudentCode(), normalized)
            || contains(student.getUserAccount().getUniversityEmail(), normalized)
            || profile != null && (
                contains(profile.getFirstName(), normalized)
                    || contains(profile.getLastName(), normalized)
                    || contains(profile.getFirstName() + " " + profile.getLastName(), normalized)
                    || contains(profile.getCin(), normalized)
            );
    }

    private boolean matchesAcademicPlacement(
        AcademicRegistration registration,
        UUID semesterId,
        UUID classGroupId
    ) {
        if (semesterId == null && classGroupId == null) {
            return true;
        }
        return semesterRegistrationRepository
            .findByAcademicRegistrationId(registration.getId())
            .stream()
            .filter(semesterRegistration -> semesterId == null
                || semesterId.equals(semesterRegistration.getSemester().getId()))
            .anyMatch(semesterRegistration -> classGroupId == null
                || studentClassAssignmentRepository
                    .findBySemesterRegistrationId(semesterRegistration.getId())
                    .map(assignment -> classGroupId.equals(assignment.getClassGroup().getId()))
                    .orElse(false));
    }

    private boolean contains(String value, String normalizedQuery) {
        return value != null && value.toLowerCase().contains(normalizedQuery);
    }

    private void ensureRegistrationContext(
        Establishment establishment,
        Student student,
        ProgramFiliere programFiliere,
        AcademicLevel academicLevel,
        AcademicYear academicYear
    ) {
        UUID establishmentId = establishment.getId();
        UUID programEstablishmentId = programFiliere
            .getDepartment()
            .getEstablishment()
            .getId();

        if (!establishmentId.equals(student.getEstablishment().getId())
            || !establishmentId.equals(programEstablishmentId)
            || !establishmentId.equals(academicYear.getEstablishment().getId())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Student, program/filiere, and academic year must belong to the establishment"
            );
        }

        if (!programFiliere.getId().equals(academicLevel.getProgramFiliere().getId())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Academic level must belong to the selected program/filiere"
            );
        }
    }

    private void ensureStudentNotRegistered(UUID studentId, UUID academicYearId) {
        if (academicRegistrationRepository.existsByStudentIdAndAcademicYearId(
            studentId,
            academicYearId
        )) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Student is already registered for this academic year"
            );
        }
    }

    private void ensureActiveRuleAssignment(UUID academicLevelId, UUID academicYearId) {
        if (!ruleAssignmentRepository.existsByAcademicLevelIdAndAcademicYearIdAndStatus(
            academicLevelId,
            academicYearId,
            AcademicLevelRuleAssignmentStatus.ACTIVE
        )) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Academic level requires an active rule assignment for this academic year"
            );
        }
    }

    private AcademicRegistrationResponse toResponse(
        AcademicRegistration academicRegistration
    ) {
        return new AcademicRegistrationResponse(
            academicRegistration.getId(),
            academicRegistration.getStudent().getId(),
            academicRegistration.getStudent().getEstablishment().getId(),
            academicRegistration.getProgramFiliere().getId(),
            academicRegistration.getAcademicLevel().getId(),
            academicRegistration.getAcademicYear().getId(),
            academicRegistration.getStatus(),
            academicRegistration.getCreatedAt(),
            academicRegistration.getUpdatedAt()
        );
    }
}
