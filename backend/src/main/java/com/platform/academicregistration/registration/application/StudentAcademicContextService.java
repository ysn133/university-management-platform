package com.platform.academicregistration.registration.application;

import com.platform.academicregistration.classassignment.infrastructure.StudentClassAssignmentRepository;
import com.platform.academicregistration.registration.domain.AcademicRegistration;
import com.platform.academicregistration.registration.infrastructure.AcademicRegistrationRepository;
import com.platform.academicregistration.registration.presentation.dto.StudentAcademicContextResponse;
import com.platform.academicregistration.semesterregistration.domain.SemesterRegistration;
import com.platform.academicregistration.semesterregistration.infrastructure.SemesterRegistrationRepository;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.teachinggroup.domain.TeachingGroupType;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupMembershipRepository;
import com.platform.universitygovernance.programfiliere.domain.ProgramFiliere;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudentAcademicContextService {

    private final AcademicRegistrationRepository academicRegistrationRepository;
    private final SemesterRegistrationRepository semesterRegistrationRepository;
    private final StudentClassAssignmentRepository classAssignmentRepository;
    private final TeachingGroupMembershipRepository teachingGroupMembershipRepository;

    public StudentAcademicContextService(
        AcademicRegistrationRepository academicRegistrationRepository,
        SemesterRegistrationRepository semesterRegistrationRepository,
        StudentClassAssignmentRepository classAssignmentRepository,
        TeachingGroupMembershipRepository teachingGroupMembershipRepository
    ) {
        this.academicRegistrationRepository = academicRegistrationRepository;
        this.semesterRegistrationRepository = semesterRegistrationRepository;
        this.classAssignmentRepository = classAssignmentRepository;
        this.teachingGroupMembershipRepository = teachingGroupMembershipRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentAcademicContextResponse> getContexts(AuthenticatedUserPrincipal principal) {
        if (principal.role() != AccountRoleType.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
        }

        return academicRegistrationRepository
            .findByStudentIdOrderByAcademicYearStartYearDesc(principal.roleEntityId())
            .stream()
            .flatMap(registration -> semesterRegistrationRepository
                .findByAcademicRegistrationId(registration.getId())
                .stream()
                .map(semesterRegistration -> toResponse(registration, semesterRegistration)))
            .sorted(Comparator.comparing(
                StudentAcademicContextResponse::semesterStartDate,
                Comparator.reverseOrder()
            ))
            .toList();
    }

    private StudentAcademicContextResponse toResponse(
        AcademicRegistration registration,
        SemesterRegistration semesterRegistration
    ) {
        var semester = semesterRegistration.getSemester();
        var classAssignment = classAssignmentRepository
            .findBySemesterRegistrationId(semesterRegistration.getId())
            .orElse(null);
        var memberships = teachingGroupMembershipRepository
            .findByTeachingGroupSemesterIdAndSemesterRegistrationId(
                semester.getId(),
                semesterRegistration.getId()
            );
        ProgramFiliere program = registration.getProgramFiliere();

        return new StudentAcademicContextResponse(
            registration.getId(),
            semesterRegistration.getId(),
            registration.getAcademicYear().getId(),
            registration.getAcademicYear().getLabel(),
            registration.getAcademicYear().getStatus().name(),
            program.getProgramPath().getId(),
            program.getProgramPath().getName(),
            program.getId(),
            program.getCode(),
            program.getName(),
            registration.getAcademicLevel().getId(),
            registration.getAcademicLevel().getName(),
            semester.getId(),
            semester.getName(),
            semester.getStartDate(),
            semester.getEndDate(),
            registration.getStatus().name(),
            classAssignment == null ? null : classAssignment.getClassGroup().getId(),
            classAssignment == null ? null : classAssignment.getClassGroup().getName(),
            groupNames(memberships, TeachingGroupType.TD),
            groupNames(memberships, TeachingGroupType.TP)
        );
    }

    private List<String> groupNames(
        List<com.platform.scheduling.teachinggroup.domain.TeachingGroupMembership> memberships,
        TeachingGroupType type
    ) {
        return memberships.stream()
            .filter(membership -> type == membership.getTeachingGroup().getGroupType())
            .map(membership -> membership.getTeachingGroup().getName())
            .distinct()
            .sorted()
            .toList();
    }
}
