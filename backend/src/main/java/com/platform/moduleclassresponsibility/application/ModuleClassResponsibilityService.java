package com.platform.moduleclassresponsibility.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.Professor;
import com.platform.identityaccess.infrastructure.ProfessorRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.moduleclassresponsibility.domain.ModuleClassResponsibility;
import com.platform.moduleclassresponsibility.domain.ModuleClassResponsibilityStatus;
import com.platform.moduleclassresponsibility.infrastructure.ModuleClassResponsibilityRepository;
import com.platform.moduleclassresponsibility.presentation.dto.CreateModuleClassResponsibilityRequest;
import com.platform.moduleclassresponsibility.presentation.dto.ModuleClassResponsibilityResponse;
import com.platform.teachingassignment.domain.TeachingAssignment;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
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
public class ModuleClassResponsibilityService {

    private final ModuleClassResponsibilityRepository responsibilityRepository;
    private final ProfessorRepository professorRepository;
    private final SubjectModuleRepository subjectModuleRepository;
    private final ClassGroupRepository classGroupRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;
    private final EstablishmentRepository establishmentRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public ModuleClassResponsibilityService(
        ModuleClassResponsibilityRepository responsibilityRepository,
        ProfessorRepository professorRepository,
        SubjectModuleRepository subjectModuleRepository,
        ClassGroupRepository classGroupRepository,
        AcademicYearRepository academicYearRepository,
        SemesterRepository semesterRepository,
        EstablishmentRepository establishmentRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.responsibilityRepository = responsibilityRepository;
        this.professorRepository = professorRepository;
        this.subjectModuleRepository = subjectModuleRepository;
        this.classGroupRepository = classGroupRepository;
        this.academicYearRepository = academicYearRepository;
        this.semesterRepository = semesterRepository;
        this.establishmentRepository = establishmentRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public ModuleClassResponsibilityResponse createResponsibility(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        CreateModuleClassResponsibilityRequest request
    ) {
        requirePermission(
            principal,
            establishmentId,
            PermissionCode.MODULE_CLASS_RESPONSIBILITY_CREATE
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

        responsibilityRepository
            .findBySubjectModuleIdAndClassGroupIdAndAcademicYearIdAndSemesterIdAndStatus(
                subjectModule.getId(),
                classGroup.getId(),
                academicYear.getId(),
                semester.getId(),
                ModuleClassResponsibilityStatus.ACTIVE
            )
            .ifPresent(existing -> {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This module and class group already have an active responsible professor"
                );
            });

        ModuleClassResponsibility responsibility = responsibilityRepository
            .findByProfessorIdAndSubjectModuleIdAndClassGroupIdAndAcademicYearIdAndSemesterId(
                professor.getId(),
                subjectModule.getId(),
                classGroup.getId(),
                academicYear.getId(),
                semester.getId()
            )
            .orElseGet(ModuleClassResponsibility::new);

        responsibility.setProfessor(professor);
        responsibility.setSubjectModule(subjectModule);
        responsibility.setClassGroup(classGroup);
        responsibility.setAcademicYear(academicYear);
        responsibility.setSemester(semester);
        responsibility.setStatus(ModuleClassResponsibilityStatus.ACTIVE);
        return toResponse(responsibilityRepository.save(responsibility));
    }

    @Transactional(readOnly = true)
    public List<ModuleClassResponsibilityResponse> getResponsibilities(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        requirePermission(
            principal,
            establishmentId,
            PermissionCode.MODULE_CLASS_RESPONSIBILITY_VIEW
        );
        findEstablishment(establishmentId);
        return responsibilityRepository
            .findByProfessorEstablishmentIdOrderByCreatedAtDesc(establishmentId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ModuleClassResponsibilityResponse getResponsibility(
        AuthenticatedUserPrincipal principal,
        UUID responsibilityId
    ) {
        ModuleClassResponsibility responsibility = findResponsibility(responsibilityId);
        if (principal != null
            && principal.role() == AccountRoleType.PROFESSOR
            && principal.roleEntityId().equals(responsibility.getProfessor().getId())) {
            return toResponse(responsibility);
        }

        requirePermission(
            principal,
            establishmentId(responsibility),
            PermissionCode.MODULE_CLASS_RESPONSIBILITY_VIEW
        );
        return toResponse(responsibility);
    }

    @Transactional(readOnly = true)
    public List<ModuleClassResponsibilityResponse> getMyResponsibilities(
        AuthenticatedUserPrincipal principal
    ) {
        if (principal == null || principal.role() != AccountRoleType.PROFESSOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Professor access required");
        }
        return responsibilityRepository
            .findByProfessorIdOrderByCreatedAtDesc(principal.roleEntityId())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ActionResponse removeResponsibility(
        AuthenticatedUserPrincipal principal,
        UUID responsibilityId
    ) {
        ModuleClassResponsibility responsibility = findResponsibility(responsibilityId);
        requirePermission(
            principal,
            establishmentId(responsibility),
            PermissionCode.MODULE_CLASS_RESPONSIBILITY_DELETE
        );

        responsibility.setStatus(ModuleClassResponsibilityStatus.INACTIVE);
        responsibilityRepository.save(responsibility);
        return new ActionResponse(true, "Module class responsibility removed");
    }

    @Transactional
    public void synchronizeWithCourseAssignment(TeachingAssignment assignment) {
        var requirement = assignment.getTeachingRequirement();
        var component = requirement.getModuleTeachingComponent();
        ClassGroup classGroup = requirement.getTeachingGroup().getSourceClassGroup();
        if (component.getComponentType() != TeachingComponentType.COURSE
            || classGroup == null) {
            return;
        }

        Professor professor = assignment.getProfessor();
        SubjectModule subjectModule = component.getSubjectModule();
        Semester semester = requirement.getTeachingGroup().getSemester();
        AcademicYear academicYear = semester.getAcademicYear();

        ModuleClassResponsibility active = responsibilityRepository
            .findBySubjectModuleIdAndClassGroupIdAndAcademicYearIdAndSemesterIdAndStatus(
                subjectModule.getId(),
                classGroup.getId(),
                academicYear.getId(),
                semester.getId(),
                ModuleClassResponsibilityStatus.ACTIVE
            )
            .orElse(null);
        if (active != null && active.getProfessor().getId().equals(professor.getId())) {
            return;
        }

        ModuleClassResponsibility matching = responsibilityRepository
            .findByProfessorIdAndSubjectModuleIdAndClassGroupIdAndAcademicYearIdAndSemesterId(
                professor.getId(),
                subjectModule.getId(),
                classGroup.getId(),
                academicYear.getId(),
                semester.getId()
            )
            .orElse(null);

        if (matching != null) {
            if (active != null) {
                active.setStatus(ModuleClassResponsibilityStatus.INACTIVE);
                responsibilityRepository.save(active);
            }
            matching.setStatus(ModuleClassResponsibilityStatus.ACTIVE);
            responsibilityRepository.save(matching);
            return;
        }

        ModuleClassResponsibility responsibility = active == null
            ? new ModuleClassResponsibility()
            : active;
        responsibility.setProfessor(professor);
        responsibility.setSubjectModule(subjectModule);
        responsibility.setClassGroup(classGroup);
        responsibility.setAcademicYear(academicYear);
        responsibility.setSemester(semester);
        responsibility.setStatus(ModuleClassResponsibilityStatus.ACTIVE);
        responsibilityRepository.save(responsibility);
    }

    private ModuleClassResponsibility findResponsibility(UUID responsibilityId) {
        return responsibilityRepository.findById(responsibilityId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Module class responsibility not found"
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
                "Module class responsibilities require an active establishment, professor, class group, and open academic year"
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

    private UUID establishmentId(ModuleClassResponsibility responsibility) {
        return responsibility.getProfessor().getEstablishment().getId();
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

    private ModuleClassResponsibilityResponse toResponse(
        ModuleClassResponsibility responsibility
    ) {
        return new ModuleClassResponsibilityResponse(
            responsibility.getId(),
            establishmentId(responsibility),
            responsibility.getProfessor().getId(),
            responsibility.getSubjectModule().getId(),
            responsibility.getClassGroup().getId(),
            responsibility.getAcademicYear().getId(),
            responsibility.getSemester().getId(),
            responsibility.getStatus(),
            responsibility.getCreatedAt(),
            responsibility.getUpdatedAt()
        );
    }
}
