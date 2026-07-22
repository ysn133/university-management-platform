package com.platform.academicregistration.registration.application;

import com.platform.academicregistration.registration.domain.AcademicRegistration;
import com.platform.academicregistration.registration.domain.AcademicRegistrationStatus;
import com.platform.academicregistration.registration.infrastructure.AcademicRegistrationRepository;
import com.platform.academicregistration.registration.presentation.dto.AcademicRegistrationResponse;
import com.platform.academicregistration.registration.presentation.dto.CreateAcademicRegistrationRequest;
import com.platform.academicregistration.registration.presentation.dto.UpdateAcademicRegistrationRequest;
import com.platform.academicregistration.semesterregistration.application.SemesterRegestrationService;
import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.Student;
import com.platform.identityaccess.infrastructure.StudentRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academiclevel.infrastructure.AcademicLevelRepository;
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
    private final SemesterRegestrationService semesterRegestrationService;

    public AcademicRegistrationService(
        AcademicRegistrationRepository academicRegistrationRepository,
        StudentRepository studentRepository,
        ProgramFiliereRepository programFiliereRepository,
        AcademicLevelRepository academicLevelRepository,
        AcademicYearRepository academicYearRepository,
        EstablishmentRepository establishmentRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService,
        SemesterRegestrationService semesterRegestrationService

    ) {
        this.academicRegistrationRepository = academicRegistrationRepository;
        this.studentRepository = studentRepository;
        this.programFiliereRepository = programFiliereRepository;
        this.academicLevelRepository = academicLevelRepository;
        this.academicYearRepository = academicYearRepository;
        this.establishmentRepository = establishmentRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
        this.semesterRegestrationService = semesterRegestrationService;

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
        ensureStudentNotRegistered(student.getId(), academicYear.getId());

        AcademicRegistration academicRegistration = new AcademicRegistration();
        academicRegistration.setStudent(student);
        academicRegistration.setProgramFiliere(programFiliere);
        academicRegistration.setAcademicLevel(academicLevel);
        academicRegistration.setAcademicYear(academicYear);
        academicRegistration.setStatus(AcademicRegistrationStatus.ACTIVE);
        AcademicRegistration savedRegistration=academicRegistrationRepository.save(academicRegistration);

        semesterRegestrationService.createSemesterRegestration(savedRegistration);


        return toResponse(savedRegistration);
    }

    @Transactional(readOnly = true)
    public List<AcademicRegistrationResponse> getAcademicRegistrations(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
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
            .map(this::toResponse)
            .toList();
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

    private UUID establishmentId(AcademicRegistration academicRegistration) {
        return academicRegistration.getStudent().getEstablishment().getId();
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
