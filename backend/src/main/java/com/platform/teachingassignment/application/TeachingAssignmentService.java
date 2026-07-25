package com.platform.teachingassignment.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.Professor;
import com.platform.identityaccess.infrastructure.ProfessorRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.teachingassignment.domain.TeachingAssignment;
import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import com.platform.teachingassignment.infrastructure.TeachingAssignmentRepository;
import com.platform.teachingassignment.presentation.dto.CreateTeachingAssignmentRequest;
import com.platform.teachingassignment.presentation.dto.TeachingAssignmentResponse;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicyear.domain.AcademicYearStatus;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.classgroup.domain.ClassGroup;
import com.platform.universitygovernance.classgroup.domain.ClassGroupStatus;
import com.platform.universitygovernance.classgroup.infrastructure.ClassGroupRepository;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.domain.EstablishmentStatus;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.universitygovernance.semester.domain.Semester;
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;
import com.platform.universitygovernance.subjectmodules.domain.SubjectModule;
import com.platform.universitygovernance.subjectmodules.infrastructure.SubjectModuleRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TeachingAssignmentService {

    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final ProfessorRepository professorRepository;
    private final SubjectModuleRepository subjectModuleRepository;
    private final ClassGroupRepository classGroupRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;
    private final EstablishmentRepository establishmentRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public TeachingAssignmentService(
        TeachingAssignmentRepository teachingAssignmentRepository,
        ProfessorRepository professorRepository,
        SubjectModuleRepository subjectModuleRepository,
        ClassGroupRepository classGroupRepository,
        AcademicYearRepository academicYearRepository,
        SemesterRepository semesterRepository,
        EstablishmentRepository establishmentRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.teachingAssignmentRepository = teachingAssignmentRepository;
        this.professorRepository = professorRepository;
        this.subjectModuleRepository = subjectModuleRepository;
        this.classGroupRepository = classGroupRepository;
        this.academicYearRepository = academicYearRepository;
        this.semesterRepository = semesterRepository;
        this.establishmentRepository = establishmentRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public TeachingAssignmentResponse createTeachingAssignment(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        CreateTeachingAssignmentRequest request
    ) {
        requirePermission(
            principal,
            establishmentId,
            PermissionCode.TEACHING_ASSIGNMENT_CREATE
        );
        Establishment establishment = findEstablishment(establishmentId);

        Professor professor = findProfessor(request.professorId());
        SubjectModule subjectModule = findSubjectModule(request.subjectModuleId());
        ClassGroup classGroup = findClassGroup(request.classGroupId());
        AcademicYear academicYear = findAcademicYear(request.academicYearId());
        Semester semester = findSemester(request.semesterId());
        ensureCompatibleContext(
            establishmentId,
            professor,
            subjectModule,
            classGroup,
            academicYear,
            semester
        );
        ensureAssignableState(establishment, professor, classGroup, academicYear);

        teachingAssignmentRepository
            .findBySubjectModuleIdAndClassGroupIdAndAcademicYearIdAndSemesterIdAndStatus(
                subjectModule.getId(),
                classGroup.getId(),
                academicYear.getId(),
                semester.getId(),
                TeachingAssignmentStatus.ACTIVE
            )
            .ifPresent(existing -> {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This module and class group already have an active professor assignment for the academic period"
                );
            });

        TeachingAssignment assignment = teachingAssignmentRepository
            .findByProfessorIdAndSubjectModuleIdAndClassGroupIdAndAcademicYearIdAndSemesterId(
                professor.getId(),
                subjectModule.getId(),
                classGroup.getId(),
                academicYear.getId(),
                semester.getId()
            )
            .orElseGet(TeachingAssignment::new);

        assignment.setProfessor(professor);
        assignment.setSubjectModule(subjectModule);
        assignment.setClassGroup(classGroup);
        assignment.setAcademicYear(academicYear);
        assignment.setSemester(semester);
        assignment.setStatus(TeachingAssignmentStatus.ACTIVE);
        return toResponse(teachingAssignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<TeachingAssignmentResponse> getTeachingAssignments(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        requirePermission(
            principal,
            establishmentId,
            PermissionCode.TEACHING_ASSIGNMENT_VIEW
        );
        findEstablishment(establishmentId);
        return teachingAssignmentRepository
            .findByProfessorEstablishmentIdOrderByCreatedAtDesc(establishmentId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TeachingAssignmentResponse getTeachingAssignment(
        AuthenticatedUserPrincipal principal,
        UUID teachingAssignmentId
    ) {
        TeachingAssignment assignment = findTeachingAssignment(teachingAssignmentId);
        if (principal != null
            && principal.role() == AccountRoleType.PROFESSOR
            && principal.roleEntityId().equals(assignment.getProfessor().getId())) {
            return toResponse(assignment);
        }

        requirePermission(
            principal,
            establishmentId(assignment),
            PermissionCode.TEACHING_ASSIGNMENT_VIEW
        );
        return toResponse(assignment);
    }

    @Transactional(readOnly = true)
    public List<TeachingAssignmentResponse> getMyTeachingAssignments(
        AuthenticatedUserPrincipal principal
    ) {
        if (principal == null || principal.role() != AccountRoleType.PROFESSOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Professor access required");
        }
        return teachingAssignmentRepository
            .findByProfessorIdOrderByCreatedAtDesc(principal.roleEntityId())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ActionResponse unassignProfessor(
        AuthenticatedUserPrincipal principal,
        UUID teachingAssignmentId
    ) {
        TeachingAssignment assignment = findTeachingAssignment(teachingAssignmentId);
        requirePermission(
            principal,
            establishmentId(assignment),
            PermissionCode.TEACHING_ASSIGNMENT_DELETE
        );

        assignment.setStatus(TeachingAssignmentStatus.INACTIVE);
        teachingAssignmentRepository.save(assignment);
        return new ActionResponse(true, "Professor unassigned");
    }

    private TeachingAssignment findTeachingAssignment(UUID teachingAssignmentId) {
        return teachingAssignmentRepository.findById(teachingAssignmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Teaching assignment not found"
            ));
    }

    private Professor findProfessor(UUID professorId) {
        return professorRepository.findById(professorId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Professor not found"
            ));
    }

    private SubjectModule findSubjectModule(UUID subjectModuleId) {
        return subjectModuleRepository.findById(subjectModuleId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Subject module not found"
            ));
    }

    private ClassGroup findClassGroup(UUID classGroupId) {
        return classGroupRepository.findByIdForUpdate(classGroupId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Class group not found"
            ));
    }

    private AcademicYear findAcademicYear(UUID academicYearId) {
        return academicYearRepository.findById(academicYearId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic year not found"
            ));
    }

    private Semester findSemester(UUID semesterId) {
        return semesterRepository.findById(semesterId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Semester not found"
            ));
    }

    private Establishment findEstablishment(UUID establishmentId) {
        return establishmentRepository.findById(establishmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Establishment not found"
            ));
    }

    private void ensureAssignableState(
        Establishment establishment,
        Professor professor,
        ClassGroup classGroup,
        AcademicYear academicYear
    ) {
        boolean assignable = establishment.getEstablishmentStatus()
                == EstablishmentStatus.ACTIVE
            && professor.getUserAccount().getAccountStatus() == AccountStatus.ACTIVE
            && classGroup.getStatus() == ClassGroupStatus.ACTIVE
            && academicYear.getStatus() != AcademicYearStatus.CLOSED;

        if (!assignable) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Teaching assignments require an active establishment, professor, class group, and open academic year"
            );
        }
    }

    private void ensureCompatibleContext(
        UUID establishmentId,
        Professor professor,
        SubjectModule subjectModule,
        ClassGroup classGroup,
        AcademicYear academicYear,
        Semester semester
    ) {
        UUID semesterEstablishmentId = semester
            .getAcademicLevel()
            .getProgramFiliere()
            .getDepartment()
            .getEstablishment()
            .getId();
        UUID classGroupEstablishmentId = classGroup
            .getAcademicLevel()
            .getProgramFiliere()
            .getDepartment()
            .getEstablishment()
            .getId();

        boolean compatible = establishmentId.equals(
                professor.getEstablishment().getId()
            )
            && establishmentId.equals(academicYear.getEstablishment().getId())
            && establishmentId.equals(semesterEstablishmentId)
            && establishmentId.equals(classGroupEstablishmentId)
            && semester.getId().equals(subjectModule.getSemester().getId())
            && academicYear.getId().equals(semester.getAcademicYear().getId())
            && academicYear.getId().equals(classGroup.getAcademicYear().getId())
            && semester.getAcademicLevel().getId().equals(
                classGroup.getAcademicLevel().getId()
            );

        if (!compatible) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Professor, module, class group, academic year, and semester must belong to the same academic context"
            );
        }
    }

    private UUID establishmentId(TeachingAssignment assignment) {
        return assignment.getProfessor().getEstablishment().getId();
    }

    private void requirePermission(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        PermissionCode permissionCode
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            permissionCode
        );
    }

    private TeachingAssignmentResponse toResponse(TeachingAssignment assignment) {
        return new TeachingAssignmentResponse(
            assignment.getId(),
            establishmentId(assignment),
            assignment.getProfessor().getId(),
            assignment.getSubjectModule().getId(),
            assignment.getClassGroup().getId(),
            assignment.getAcademicYear().getId(),
            assignment.getSemester().getId(),
            assignment.getStatus(),
            assignment.getCreatedAt(),
            assignment.getUpdatedAt()
        );
    }
}
