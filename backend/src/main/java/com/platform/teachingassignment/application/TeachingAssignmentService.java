package com.platform.teachingassignment.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.Professor;
import com.platform.identityaccess.domain.Student;
import com.platform.identityaccess.domain.UserProfile;
import com.platform.identityaccess.infrastructure.ProfessorRepository;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.moduleclassresponsibility.application.ModuleClassResponsibilityService;
import com.platform.shared.presentation.ActionResponse;
import com.platform.teachingassignment.domain.TeachingAssignment;
import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import com.platform.teachingassignment.domain.TeachingAssignmentSource;
import com.platform.teachingassignment.infrastructure.TeachingAssignmentRepository;
import com.platform.teachingassignment.presentation.dto.CreateTeachingAssignmentRequest;
import com.platform.teachingassignment.presentation.dto.TeachingAssignmentResponse;
import com.platform.teachingassignment.presentation.dto.TeachingAssignmentStudentResponse;
import com.platform.teachingrequirement.domain.TeachingRequirement;
import com.platform.teachingrequirement.domain.TeachingRequirementStatus;
import com.platform.teachingrequirement.infrastructure.TeachingRequirementRepository;
import com.platform.universitygovernance.academicyear.domain.AcademicYearStatus;
import com.platform.universitygovernance.moduleteachingcomponent.domain.ModuleTeachingComponent;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import com.platform.universitygovernance.semester.domain.SemesterLifecycleStatus;
import com.platform.universitygovernance.subjectmodules.infrastructure.SubjectModuleDomainRepository;
import com.platform.usermanagement.professor.expertise.infrastructure.ProfessorExpertiseRepository;
import com.platform.scheduling.teachinggroup.domain.TeachingGroupMembership;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupMembershipRepository;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TeachingAssignmentService {

    private final TeachingAssignmentRepository assignmentRepository;
    private final TeachingRequirementRepository requirementRepository;
    private final ProfessorRepository professorRepository;
    private final SubjectModuleDomainRepository subjectModuleDomainRepository;
    private final ProfessorExpertiseRepository professorExpertiseRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;
    private final TeachingGroupMembershipRepository teachingGroupMembershipRepository;
    private final UserProfileRepository userProfileRepository;
    private final ModuleClassResponsibilityService responsibilityService;

    public TeachingAssignmentService(
        TeachingAssignmentRepository assignmentRepository,
        TeachingRequirementRepository requirementRepository,
        ProfessorRepository professorRepository,
        SubjectModuleDomainRepository subjectModuleDomainRepository,
        ProfessorExpertiseRepository professorExpertiseRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService,
        TeachingGroupMembershipRepository teachingGroupMembershipRepository,
        UserProfileRepository userProfileRepository,
        ModuleClassResponsibilityService responsibilityService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.requirementRepository = requirementRepository;
        this.professorRepository = professorRepository;
        this.subjectModuleDomainRepository = subjectModuleDomainRepository;
        this.professorExpertiseRepository = professorExpertiseRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
        this.teachingGroupMembershipRepository = teachingGroupMembershipRepository;
        this.userProfileRepository = userProfileRepository;
        this.responsibilityService = responsibilityService;
    }

    @Transactional
    public TeachingAssignmentResponse createTeachingAssignment(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        CreateTeachingAssignmentRequest request
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.TEACHING_ASSIGNMENT_CREATE
        );
        TeachingRequirement requirement = findRequirement(request.teachingRequirementId());
        Professor professor = findProfessor(request.professorId());
        ensureAssignable(establishmentId, professor, requirement);

        assignmentRepository.findByTeachingRequirementIdAndStatus(
            requirement.getId(),
            TeachingAssignmentStatus.ACTIVE
        ).ifPresent(existing -> {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "This teaching requirement already has an active professor"
            );
        });

        TeachingAssignment assignment = assignmentRepository
            .findByProfessorIdAndTeachingRequirementId(
                professor.getId(),
                requirement.getId()
            )
            .orElseGet(TeachingAssignment::new);
        assignment.setProfessor(professor);
        assignment.setTeachingRequirement(requirement);
        assignment.setStatus(TeachingAssignmentStatus.ACTIVE);
        assignment.setAssignmentSource(TeachingAssignmentSource.MANUAL);
        ensureWeeklyWorkload(professor, assignment);
        assignment = assignmentRepository.save(assignment);
        responsibilityService.synchronizeWithCourseAssignment(assignment);
        return toResponse(assignment);
    }

    @Transactional(readOnly = true)
    public List<TeachingAssignmentResponse> getTeachingAssignments(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.TEACHING_ASSIGNMENT_VIEW
        );
        return assignmentRepository
            .findByTeachingRequirementTeachingGroupSemesterAcademicLevelProgramFiliereDepartmentEstablishmentIdOrderByCreatedAtDesc(
                establishmentId
            )
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TeachingAssignmentResponse getTeachingAssignment(
        AuthenticatedUserPrincipal principal,
        UUID teachingAssignmentId
    ) {
        TeachingAssignment assignment = findAssignment(teachingAssignmentId);
        if (!isAssignedProfessor(principal, assignment)) {
            permissionAuthorizationService.requirePermission(
                principal,
                establishmentId(assignment),
                PermissionCode.TEACHING_ASSIGNMENT_VIEW
            );
        }
        return toResponse(assignment);
    }

    @Transactional(readOnly = true)
    public List<TeachingAssignmentResponse> getMyTeachingAssignments(
        AuthenticatedUserPrincipal principal
    ) {
        if (principal == null || principal.role() != AccountRoleType.PROFESSOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Professor access required");
        }
        return assignmentRepository
            .findByProfessorIdOrderByCreatedAtDesc(principal.roleEntityId())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TeachingAssignmentStudentResponse> getTeachingAssignmentStudents(
        AuthenticatedUserPrincipal principal,
        UUID teachingAssignmentId
    ) {
        TeachingAssignment assignment = findAssignment(teachingAssignmentId);
        if (!isAssignedProfessor(principal, assignment)) {
            permissionAuthorizationService.requirePermission(
                principal,
                establishmentId(assignment),
                PermissionCode.TEACHING_ASSIGNMENT_VIEW
            );
        }
        return teachingGroupMembershipRepository
            .findByTeachingGroupId(assignment.getTeachingRequirement().getTeachingGroup().getId())
            .stream()
            .map(TeachingGroupMembership::getSemesterRegistration)
            .map(registration -> registration.getAcademicRegistration().getStudent())
            .distinct()
            .map(this::toStudentResponse)
            .sorted(java.util.Comparator
                .comparing(TeachingAssignmentStudentResponse::lastName)
                .thenComparing(TeachingAssignmentStudentResponse::firstName))
            .toList();
    }

    @Transactional
    public ActionResponse unassignProfessor(
        AuthenticatedUserPrincipal principal,
        UUID teachingAssignmentId
    ) {
        TeachingAssignment assignment = findAssignment(teachingAssignmentId);
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId(assignment),
            PermissionCode.TEACHING_ASSIGNMENT_DELETE
        );
        assignment.setStatus(TeachingAssignmentStatus.INACTIVE);
        assignmentRepository.save(assignment);
        return new ActionResponse(true, "Professor unassigned");
    }

    private void ensureAssignable(
        UUID establishmentId,
        Professor professor,
        TeachingRequirement requirement
    ) {
        ModuleTeachingComponent component = requirement.getModuleTeachingComponent();
        UUID requirementEstablishmentId = establishmentId(requirement);
        boolean valid = requirement.getStatus() == TeachingRequirementStatus.ACTIVE
            && professor.getUserAccount().getAccountStatus() == AccountStatus.ACTIVE
            && professor.getEstablishment().getId().equals(establishmentId)
            && requirementEstablishmentId.equals(establishmentId)
            && component.getSubjectModule().getSemester().getAcademicYear().getStatus()
                != AcademicYearStatus.CLOSED;
        if (!valid) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Professor and teaching requirement must belong to the same active academic context"
            );
        }
        if (component.getComponentType() == TeachingComponentType.COURSE
            && (professor.getAcademicRank() == null
                || !professor.getAcademicRank().canHoldModuleResponsibility())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Course professor must have a rank eligible for module responsibility"
            );
        }
        ensureExpertise(professor, component);
    }

    private void ensureExpertise(
        Professor professor,
        ModuleTeachingComponent component
    ) {
        Set<UUID> requiredDomains = subjectModuleDomainRepository
            .findBySubjectModuleId(component.getSubjectModule().getId())
            .stream()
            .map(link -> link.getAcademicDomain().getId())
            .collect(java.util.stream.Collectors.toSet());
        if (requiredDomains.isEmpty()) {
            return;
        }
        Set<UUID> professorDomains = new HashSet<>();
        professorExpertiseRepository
            .findByProfessorIdOrderByAcademicDomainNameAsc(professor.getId())
            .forEach(expertise -> professorDomains.add(expertise.getAcademicDomain().getId()));
        if (!professorDomains.containsAll(requiredDomains)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Professor does not satisfy the required academic domains"
            );
        }
    }

    private void ensureWeeklyWorkload(
        Professor professor,
        TeachingAssignment candidate
    ) {
        var semester = candidate.getTeachingRequirement().getTeachingGroup().getSemester();
        UUID establishmentId = establishmentId(candidate);
        int assignedMinutes = assignmentRepository
            .findInTeachingPeriod(
                establishmentId,
                semester.getAcademicYear().getId(),
                semester.getTermType(),
                TeachingAssignmentStatus.ACTIVE
            )
            .stream()
            .filter(existing -> existing.getProfessor().getId().equals(professor.getId()))
            .filter(existing -> candidate.getId() == null
                || !candidate.getId().equals(existing.getId()))
            .map(TeachingAssignment::getTeachingRequirement)
            .map(TeachingRequirement::getModuleTeachingComponent)
            .mapToInt(component -> component.getSessionsPerWeek()
                * component.getSessionDurationMinutes())
            .sum();
        ModuleTeachingComponent component = candidate
            .getTeachingRequirement()
            .getModuleTeachingComponent();
        int candidateMinutes = component.getSessionsPerWeek()
            * component.getSessionDurationMinutes();
        if (assignedMinutes + candidateMinutes > professor.getMaximumWeeklyTeachingMinutes()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Teaching assignment exceeds the professor's weekly workload"
            );
        }
    }

    private TeachingRequirement findRequirement(UUID teachingRequirementId) {
        return requirementRepository.findById(teachingRequirementId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Teaching requirement not found"
            ));
    }

    private Professor findProfessor(UUID professorId) {
        return professorRepository.findById(professorId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Professor not found"
            ));
    }

    private TeachingAssignment findAssignment(UUID teachingAssignmentId) {
        return assignmentRepository.findById(teachingAssignmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Teaching assignment not found"
            ));
    }

    private boolean isAssignedProfessor(
        AuthenticatedUserPrincipal principal,
        TeachingAssignment assignment
    ) {
        return principal != null
            && principal.role() == AccountRoleType.PROFESSOR
            && principal.roleEntityId().equals(assignment.getProfessor().getId());
    }

    private UUID establishmentId(TeachingRequirement requirement) {
        return requirement.getTeachingGroup()
            .getSemester()
            .getAcademicLevel()
            .getProgramFiliere()
            .getDepartment()
            .getEstablishment()
            .getId();
    }

    private UUID establishmentId(TeachingAssignment assignment) {
        return establishmentId(assignment.getTeachingRequirement());
    }

    private TeachingAssignmentResponse toResponse(TeachingAssignment assignment) {
        TeachingRequirement requirement = assignment.getTeachingRequirement();
        ModuleTeachingComponent component = requirement.getModuleTeachingComponent();
        var subjectModule = component.getSubjectModule();
        var semester = requirement.getTeachingGroup().getSemester();
        var academicLevel = semester.getAcademicLevel();
        var programFiliere = academicLevel.getProgramFiliere();
        LocalDate today = LocalDate.now();
        SemesterLifecycleStatus semesterLifecycleStatus = today.isBefore(semester.getStartDate())
            ? SemesterLifecycleStatus.PLANNED
            : today.isAfter(semester.getEndDate())
                ? SemesterLifecycleStatus.FINISHED
                : SemesterLifecycleStatus.ACTIVE;
        return new TeachingAssignmentResponse(
            assignment.getId(),
            establishmentId(assignment),
            assignment.getProfessor().getId(),
            requirement.getId(),
            subjectModule.getId(),
            subjectModule.getCode(),
            subjectModule.getTitle(),
            component.getComponentType(),
            component.getSessionsPerWeek(),
            component.getSessionDurationMinutes(),
            requirement.getTeachingGroup().getId(),
            requirement.getTeachingGroup().getName(),
            semester.getId(),
            semester.getName(),
            semester.getTermType(),
            semesterLifecycleStatus,
            semester.getAcademicYear().getId(),
            semester.getAcademicYear().getLabel(),
            semester.getAcademicYear().getStatus(),
            academicLevel.getId(),
            academicLevel.getName(),
            programFiliere.getId(),
            programFiliere.getCode(),
            programFiliere.getName(),
            assignment.getStatus(),
            assignment.getAssignmentSource(),
            assignment.getCreatedAt(),
            assignment.getUpdatedAt()
        );
    }

    private TeachingAssignmentStudentResponse toStudentResponse(Student student) {
        UserProfile profile = userProfileRepository
            .findByUserAccountId(student.getUserAccount().getId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Student profile not found"
            ));
        return new TeachingAssignmentStudentResponse(
            student.getId(),
            student.getApogeeCode(),
            student.getNationalStudentCode(),
            student.getUserAccount().getUniversityEmail(),
            profile.getFirstName(),
            profile.getLastName()
        );
    }
}
