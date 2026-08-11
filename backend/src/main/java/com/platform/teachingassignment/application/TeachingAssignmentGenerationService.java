package com.platform.teachingassignment.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.Professor;
import com.platform.identityaccess.infrastructure.ProfessorRepository;
import com.platform.moduleclassresponsibility.domain.ModuleClassResponsibilityStatus;
import com.platform.moduleclassresponsibility.infrastructure.ModuleClassResponsibilityRepository;
import com.platform.moduleclassresponsibility.application.ModuleClassResponsibilityService;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.teachingassignment.domain.TeachingAssignment;
import com.platform.teachingassignment.domain.TeachingAssignmentSource;
import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import com.platform.teachingassignment.infrastructure.TeachingAssignmentRepository;
import com.platform.teachingassignment.presentation.dto.ProfessorTeachingWorkloadResponse;
import com.platform.teachingassignment.presentation.dto.TeachingAssignmentGenerationResponse;
import com.platform.teachingassignment.presentation.dto.TeachingAssignmentResponse;
import com.platform.teachingassignment.presentation.dto.TeachingAssignmentUnresolvedReason;
import com.platform.teachingassignment.presentation.dto.UnresolvedTeachingRequirementResponse;
import com.platform.teachingassignment.rankpreference.infrastructure.TeachingAssignmentRankPreferenceRepository;
import com.platform.teachingrequirement.domain.TeachingRequirement;
import com.platform.teachingrequirement.domain.TeachingRequirementStatus;
import com.platform.teachingrequirement.infrastructure.TeachingRequirementRepository;
import com.platform.universitygovernance.academicyear.domain.AcademicYearStatus;
import com.platform.universitygovernance.moduleteachingcomponent.domain.ModuleTeachingComponent;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import com.platform.universitygovernance.semester.domain.Semester;
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;
import com.platform.universitygovernance.subjectmodules.infrastructure.SubjectModuleDomainRepository;
import com.platform.usermanagement.professor.expertise.infrastructure.ProfessorExpertiseRepository;
import com.platform.usermanagement.professor.rank.domain.AcademicRankStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TeachingAssignmentGenerationService {

    private final TeachingAssignmentRepository assignmentRepository;
    private final TeachingRequirementRepository requirementRepository;
    private final ProfessorRepository professorRepository;
    private final SemesterRepository semesterRepository;
    private final SubjectModuleDomainRepository moduleDomainRepository;
    private final ProfessorExpertiseRepository expertiseRepository;
    private final TeachingAssignmentRankPreferenceRepository preferenceRepository;
    private final ModuleClassResponsibilityRepository responsibilityRepository;
    private final AdminPermissionAuthorizationService authorizationService;
    private final ModuleClassResponsibilityService responsibilityService;

    public TeachingAssignmentGenerationService(
        TeachingAssignmentRepository assignmentRepository,
        TeachingRequirementRepository requirementRepository,
        ProfessorRepository professorRepository,
        SemesterRepository semesterRepository,
        SubjectModuleDomainRepository moduleDomainRepository,
        ProfessorExpertiseRepository expertiseRepository,
        TeachingAssignmentRankPreferenceRepository preferenceRepository,
        ModuleClassResponsibilityRepository responsibilityRepository,
        AdminPermissionAuthorizationService authorizationService,
        ModuleClassResponsibilityService responsibilityService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.requirementRepository = requirementRepository;
        this.professorRepository = professorRepository;
        this.semesterRepository = semesterRepository;
        this.moduleDomainRepository = moduleDomainRepository;
        this.expertiseRepository = expertiseRepository;
        this.preferenceRepository = preferenceRepository;
        this.responsibilityRepository = responsibilityRepository;
        this.authorizationService = authorizationService;
        this.responsibilityService = responsibilityService;
    }

    @Transactional
    public TeachingAssignmentGenerationResponse generate(
        AuthenticatedUserPrincipal principal,
        UUID semesterId
    ) {
        Semester semester = semesterRepository.findById(semesterId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Semester not found"));
        UUID establishmentId = semester.getAcademicLevel().getProgramFiliere()
            .getDepartment().getEstablishment().getId();
        authorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.TEACHING_ASSIGNMENT_CREATE
        );
        if (semester.getAcademicYear().getStatus() == AcademicYearStatus.CLOSED) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Teaching assignments cannot be generated for a closed academic year"
            );
        }

        List<TeachingRequirement> requirements = requirementRepository
            .findByTeachingGroupSemesterIdOrderByCreatedAtAsc(semesterId)
            .stream()
            .filter(requirement -> requirement.getStatus() == TeachingRequirementStatus.ACTIVE)
            .toList();
        List<TeachingAssignment> existingAssignments = assignmentRepository
            .findByTeachingRequirementTeachingGroupSemesterIdAndStatus(
                semesterId,
                TeachingAssignmentStatus.ACTIVE
            );
        existingAssignments.forEach(responsibilityService::synchronizeWithCourseAssignment);
        List<TeachingAssignment> teachingPeriodAssignments = assignmentRepository
            .findInTeachingPeriod(
                establishmentId,
                semester.getAcademicYear().getId(),
                semester.getTermType(),
                TeachingAssignmentStatus.ACTIVE
            );
        Set<UUID> assignedRequirementIds = existingAssignments.stream()
            .map(assignment -> assignment.getTeachingRequirement().getId())
            .collect(java.util.stream.Collectors.toSet());
        List<Professor> professors = professorRepository
            .findByEstablishmentIdOrderByCreatedAtAsc(establishmentId)
            .stream()
            .filter(this::isActiveProfessor)
            .toList();

        Map<UUID, Integer> workloads = initialWorkloads(professors, teachingPeriodAssignments);
        Map<UUID, Set<UUID>> expertise = professorExpertise(professors);
        Map<TeachingComponentType, Map<UUID, Integer>> rankPriorities = rankPriorities(establishmentId);
        List<RequirementCandidates> pending = requirements.stream()
            .filter(requirement -> !assignedRequirementIds.contains(requirement.getId()))
            .map(requirement -> candidates(requirement, professors, expertise))
            .sorted(requirementOrder())
            .toList();

        List<TeachingAssignment> created = new ArrayList<>();
        List<UnresolvedTeachingRequirementResponse> unresolved = new ArrayList<>();
        for (RequirementCandidates item : pending) {
            if (item.failureReason() != null) {
                unresolved.add(unresolved(item.requirement(), item.failureReason()));
                continue;
            }
            List<Professor> withinCapacity = item.professors().stream()
                .filter(professor -> workloads.getOrDefault(professor.getId(), 0)
                    + weeklyMinutes(item.requirement()) <= professor.getMaximumWeeklyTeachingMinutes())
                .sorted(candidateOrder(item.requirement(), workloads, rankPriorities))
                .toList();
            if (withinCapacity.isEmpty()) {
                unresolved.add(unresolved(
                    item.requirement(),
                    TeachingAssignmentUnresolvedReason.WORKLOAD_CAPACITY_EXCEEDED
                ));
                continue;
            }
            Professor selected = withinCapacity.get(0);
            TeachingAssignment assignment = assignmentRepository
                .findByProfessorIdAndTeachingRequirementId(
                    selected.getId(),
                    item.requirement().getId()
                )
                .orElseGet(TeachingAssignment::new);
            assignment.setProfessor(selected);
            assignment.setTeachingRequirement(item.requirement());
            assignment.setStatus(TeachingAssignmentStatus.ACTIVE);
            assignment.setAssignmentSource(TeachingAssignmentSource.AUTOMATIC);
            assignment = assignmentRepository.save(assignment);
            responsibilityService.synchronizeWithCourseAssignment(assignment);
            created.add(assignment);
            workloads.merge(selected.getId(), weeklyMinutes(item.requirement()), Integer::sum);
        }

        return new TeachingAssignmentGenerationResponse(
            semesterId,
            existingAssignments.size(),
            created.stream().map(this::toResponse).toList(),
            unresolved,
            professors.stream()
                .map(professor -> new ProfessorTeachingWorkloadResponse(
                    professor.getId(),
                    professor.getEmployeeNumber(),
                    workloads.getOrDefault(professor.getId(), 0),
                    professor.getMaximumWeeklyTeachingMinutes()
                ))
                .sorted(Comparator.comparing(ProfessorTeachingWorkloadResponse::employeeNumber))
                .toList()
        );
    }

    @Transactional
    public ActionResponse clear(
        AuthenticatedUserPrincipal principal,
        UUID semesterId
    ) {
        Semester semester = semesterRepository.findById(semesterId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Semester not found"));
        UUID establishmentId = semester.getAcademicLevel().getProgramFiliere()
            .getDepartment().getEstablishment().getId();
        authorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.TEACHING_ASSIGNMENT_DELETE
        );

        List<TeachingAssignment> assignments = assignmentRepository
            .findByTeachingRequirementTeachingGroupSemesterIdAndStatus(
                semesterId,
                TeachingAssignmentStatus.ACTIVE
            );
        assignments.forEach(assignment -> assignment.setStatus(TeachingAssignmentStatus.INACTIVE));
        assignmentRepository.saveAll(assignments);
        return new ActionResponse(
            true,
            assignments.size() + " teaching assignments cleared"
        );
    }

    private RequirementCandidates candidates(
        TeachingRequirement requirement,
        List<Professor> professors,
        Map<UUID, Set<UUID>> expertise
    ) {
        if (professors.isEmpty()) {
            return failed(requirement, TeachingAssignmentUnresolvedReason.NO_ACTIVE_PROFESSOR);
        }
        Set<UUID> requiredDomains = moduleDomainRepository
            .findBySubjectModuleId(requirement.getModuleTeachingComponent().getSubjectModule().getId())
            .stream()
            .map(link -> link.getAcademicDomain().getId())
            .collect(java.util.stream.Collectors.toSet());
        if (requiredDomains.isEmpty()) {
            return failed(
                requirement,
                TeachingAssignmentUnresolvedReason.MISSING_ACADEMIC_DOMAIN_CONFIGURATION
            );
        }
        List<Professor> matchingExpertise = professors.stream()
            .filter(professor -> expertise.getOrDefault(professor.getId(), Set.of())
                .containsAll(requiredDomains))
            .toList();
        if (matchingExpertise.isEmpty()) {
            return failed(requirement, TeachingAssignmentUnresolvedReason.NO_MATCHING_EXPERTISE);
        }
        if (componentType(requirement) == TeachingComponentType.COURSE) {
            matchingExpertise = matchingExpertise.stream()
                .filter(professor -> professor.getAcademicRank().canHoldModuleResponsibility())
                .toList();
            if (matchingExpertise.isEmpty()) {
                return failed(
                    requirement,
                    TeachingAssignmentUnresolvedReason.NO_ELIGIBLE_ACADEMIC_RANK
                );
            }
        }
        return new RequirementCandidates(requirement, matchingExpertise, null);
    }

    private Comparator<RequirementCandidates> requirementOrder() {
        return Comparator
            .comparing((RequirementCandidates item) -> item.professors().size() == 1 ? 0 : 1)
            .thenComparing(item -> componentOrder(componentType(item.requirement())))
            .thenComparingInt(item -> item.professors().size())
            .thenComparing(item -> item.requirement().getId().toString());
    }

    private Comparator<Professor> candidateOrder(
        TeachingRequirement requirement,
        Map<UUID, Integer> workloads,
        Map<TeachingComponentType, Map<UUID, Integer>> rankPriorities
    ) {
        UUID responsibleProfessorId = responsibleProfessorId(requirement);
        Map<UUID, Integer> preferences = rankPriorities.getOrDefault(
            componentType(requirement),
            Map.of()
        );
        return Comparator
            .comparing((Professor professor) -> professor.getId().equals(responsibleProfessorId) ? 0 : 1)
            .thenComparingInt(professor -> preferences.getOrDefault(
                professor.getAcademicRank().getId(),
                Integer.MAX_VALUE
            ))
            .thenComparingInt(professor -> workloads.getOrDefault(professor.getId(), 0))
            .thenComparingDouble(professor -> (double) workloads.getOrDefault(professor.getId(), 0)
                / professor.getMaximumWeeklyTeachingMinutes())
            .thenComparingInt(professor -> professor.getAcademicRank().getSeniorityOrder())
            .thenComparing(Professor::getEmployeeNumber, String.CASE_INSENSITIVE_ORDER);
    }

    private UUID responsibleProfessorId(TeachingRequirement requirement) {
        if (componentType(requirement) != TeachingComponentType.COURSE
            || requirement.getTeachingGroup().getSourceClassGroup() == null) {
            return null;
        }
        ModuleTeachingComponent component = requirement.getModuleTeachingComponent();
        Semester semester = requirement.getTeachingGroup().getSemester();
        return responsibilityRepository
            .findBySubjectModuleIdAndClassGroupIdAndAcademicYearIdAndSemesterIdAndStatus(
                component.getSubjectModule().getId(),
                requirement.getTeachingGroup().getSourceClassGroup().getId(),
                semester.getAcademicYear().getId(),
                semester.getId(),
                ModuleClassResponsibilityStatus.ACTIVE
            )
            .map(responsibility -> responsibility.getProfessor().getId())
            .orElse(null);
    }

    private Map<UUID, Integer> initialWorkloads(
        List<Professor> professors,
        List<TeachingAssignment> assignments
    ) {
        Map<UUID, Integer> workloads = new HashMap<>();
        professors.forEach(professor -> workloads.put(professor.getId(), 0));
        assignments.forEach(assignment -> workloads.merge(
            assignment.getProfessor().getId(),
            weeklyMinutes(assignment.getTeachingRequirement()),
            Integer::sum
        ));
        return workloads;
    }

    private Map<UUID, Set<UUID>> professorExpertise(List<Professor> professors) {
        Map<UUID, Set<UUID>> result = new HashMap<>();
        professors.forEach(professor -> {
            Set<UUID> domains = new HashSet<>();
            expertiseRepository.findByProfessorIdOrderByAcademicDomainNameAsc(professor.getId())
                .forEach(item -> domains.add(item.getAcademicDomain().getId()));
            result.put(professor.getId(), domains);
        });
        return result;
    }

    private Map<TeachingComponentType, Map<UUID, Integer>> rankPriorities(UUID establishmentId) {
        Map<TeachingComponentType, Map<UUID, Integer>> result = new HashMap<>();
        preferenceRepository.findByEstablishmentIdOrderByComponentTypeAscPriorityAsc(establishmentId)
            .forEach(preference -> result
                .computeIfAbsent(preference.getComponentType(), ignored -> new HashMap<>())
                .put(preference.getAcademicRank().getId(), preference.getPriority()));
        return result;
    }

    private boolean isActiveProfessor(Professor professor) {
        return professor.getUserAccount().getAccountStatus() == AccountStatus.ACTIVE
            && professor.getAcademicRank() != null
            && professor.getAcademicRank().getStatus() == AcademicRankStatus.ACTIVE;
    }

    private int weeklyMinutes(TeachingRequirement requirement) {
        ModuleTeachingComponent component = requirement.getModuleTeachingComponent();
        return component.getSessionsPerWeek() * component.getSessionDurationMinutes();
    }

    private TeachingComponentType componentType(TeachingRequirement requirement) {
        return requirement.getModuleTeachingComponent().getComponentType();
    }

    private int componentOrder(TeachingComponentType type) {
        return switch (type) {
            case COURSE -> 0;
            case TD -> 1;
            case TP -> 2;
        };
    }

    private RequirementCandidates failed(
        TeachingRequirement requirement,
        TeachingAssignmentUnresolvedReason reason
    ) {
        return new RequirementCandidates(requirement, List.of(), reason);
    }

    private UnresolvedTeachingRequirementResponse unresolved(
        TeachingRequirement requirement,
        TeachingAssignmentUnresolvedReason reason
    ) {
        return new UnresolvedTeachingRequirementResponse(
            requirement.getId(),
            requirement.getModuleTeachingComponent().getSubjectModule().getId(),
            componentType(requirement),
            requirement.getTeachingGroup().getId(),
            requirement.getTeachingGroup().getName(),
            reason,
            switch (reason) {
                case NO_ACTIVE_PROFESSOR -> "No active professor is available in the establishment";
                case MISSING_ACADEMIC_DOMAIN_CONFIGURATION -> "The subject module has no academic domain configuration";
                case NO_MATCHING_EXPERTISE -> "No professor matches all required academic domains";
                case NO_ELIGIBLE_ACADEMIC_RANK -> "No matching professor has a rank eligible for Course responsibility";
                case WORKLOAD_CAPACITY_EXCEEDED -> "All matching professors would exceed their weekly teaching capacity";
            }
        );
    }

    private TeachingAssignmentResponse toResponse(TeachingAssignment assignment) {
        TeachingRequirement requirement = assignment.getTeachingRequirement();
        ModuleTeachingComponent component = requirement.getModuleTeachingComponent();
        return new TeachingAssignmentResponse(
            assignment.getId(),
            requirement.getTeachingGroup().getSemester().getAcademicLevel()
                .getProgramFiliere().getDepartment().getEstablishment().getId(),
            assignment.getProfessor().getId(),
            requirement.getId(),
            component.getSubjectModule().getId(),
            component.getComponentType(),
            requirement.getTeachingGroup().getId(),
            requirement.getTeachingGroup().getName(),
            assignment.getStatus(),
            assignment.getAssignmentSource(),
            assignment.getCreatedAt(),
            assignment.getUpdatedAt()
        );
    }

    private record RequirementCandidates(
        TeachingRequirement requirement,
        List<Professor> professors,
        TeachingAssignmentUnresolvedReason failureReason
    ) {
    }
}
