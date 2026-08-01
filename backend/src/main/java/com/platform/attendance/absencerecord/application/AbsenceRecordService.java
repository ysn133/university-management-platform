package com.platform.attendance.absencerecord.application;

import com.platform.academicregistration.moduleregistration.domain.ModuleRegistration;
import com.platform.academicregistration.moduleregistration.domain.ModuleRegistrationStatus;
import com.platform.academicregistration.moduleregistration.infrastructure.ModuleRegistrationRepository;
import com.platform.attendance.absencerecord.domain.AbsenceRecord;
import com.platform.attendance.absencerecord.infrastructure.AbsenceRecordRepository;
import com.platform.attendance.absencerecord.presentation.dto.AbsenceRecordResponse;
import com.platform.attendance.absencerecord.presentation.dto.CreateAbsenceRequest;
import com.platform.attendance.absencerecord.presentation.dto.UpdateAbsenceJustificationRequest;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupMembershipRepository;
import com.platform.teachingassignment.domain.TeachingAssignment;
import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import com.platform.teachingassignment.infrastructure.TeachingAssignmentRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AbsenceRecordService {

    private final AbsenceRecordRepository absenceRecordRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final ModuleRegistrationRepository moduleRegistrationRepository;
    private final TeachingGroupMembershipRepository membershipRepository;

    public AbsenceRecordService(
        AbsenceRecordRepository absenceRecordRepository,
        TeachingAssignmentRepository teachingAssignmentRepository,
        ModuleRegistrationRepository moduleRegistrationRepository,
        TeachingGroupMembershipRepository membershipRepository
    ) {
        this.absenceRecordRepository = absenceRecordRepository;
        this.teachingAssignmentRepository = teachingAssignmentRepository;
        this.moduleRegistrationRepository = moduleRegistrationRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public AbsenceRecordResponse createAbsence(
        AuthenticatedUserPrincipal principal,
        UUID teachingAssignmentId,
        CreateAbsenceRequest request
    ) {
        TeachingAssignment assignment = findAssignedTeachingAssignment(
            principal,
            teachingAssignmentId
        );
        ModuleRegistration registration = findModuleRegistration(
            request.moduleRegistrationId()
        );
        validateRegistration(assignment, registration);
        validateAbsenceDate(request.absenceDate());

        if (absenceRecordRepository
            .existsByModuleRegistrationIdAndTeachingAssignmentIdAndAbsenceDate(
                registration.getId(),
                assignment.getId(),
                request.absenceDate()
            )) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "This absence has already been recorded"
            );
        }

        AbsenceRecord absence = new AbsenceRecord();
        absence.setModuleRegistration(registration);
        absence.setTeachingAssignment(assignment);
        absence.setAbsenceDate(request.absenceDate());
        absence.setJustified(false);
        return toResponse(absenceRecordRepository.save(absence));
    }

    @Transactional(readOnly = true)
    public List<AbsenceRecordResponse> getTeachingAssignmentAbsences(
        AuthenticatedUserPrincipal principal,
        UUID teachingAssignmentId
    ) {
        findAssignedTeachingAssignment(principal, teachingAssignmentId);
        return absenceRecordRepository
            .findByTeachingAssignmentIdOrderByAbsenceDateDesc(teachingAssignmentId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AbsenceRecordResponse> getMyAbsences(
        AuthenticatedUserPrincipal principal
    ) {
        if (principal == null || principal.role() != AccountRoleType.STUDENT) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Student access required"
            );
        }
        return absenceRecordRepository
            .findByModuleRegistrationSemesterRegistrationAcademicRegistrationStudentIdOrderByAbsenceDateDesc(
                principal.roleEntityId()
            )
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public AbsenceRecordResponse updateJustification(
        AuthenticatedUserPrincipal principal,
        UUID absenceId,
        UpdateAbsenceJustificationRequest request
    ) {
        AbsenceRecord absence = findAbsence(absenceId);
        findAssignedTeachingAssignment(
            principal,
            absence.getTeachingAssignment().getId()
        );
        absence.setJustified(request.justified());
        absence.setJustificationNote(
            request.justified() ? normalizeNote(request.justificationNote()) : null
        );
        return toResponse(absenceRecordRepository.save(absence));
    }

    private TeachingAssignment findAssignedTeachingAssignment(
        AuthenticatedUserPrincipal principal,
        UUID teachingAssignmentId
    ) {
        TeachingAssignment assignment = teachingAssignmentRepository
            .findById(teachingAssignmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Teaching assignment not found"
            ));
        boolean assignedProfessor = principal != null
            && principal.role() == AccountRoleType.PROFESSOR
            && principal.roleEntityId().equals(assignment.getProfessor().getId())
            && assignment.getStatus() == TeachingAssignmentStatus.ACTIVE;
        if (!assignedProfessor) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Assigned professor access required"
            );
        }
        return assignment;
    }

    private ModuleRegistration findModuleRegistration(UUID registrationId) {
        return moduleRegistrationRepository.findById(registrationId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Module registration not found"
            ));
    }

    private AbsenceRecord findAbsence(UUID absenceId) {
        return absenceRecordRepository.findById(absenceId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Absence not found"
            ));
    }

    private void validateRegistration(
        TeachingAssignment assignment,
        ModuleRegistration registration
    ) {
        boolean sameAcademicContext = registration.getStatus()
                == ModuleRegistrationStatus.ACTIVE
            && registration.getSubjectModule().getId().equals(
                assignment.getTeachingRequirement()
                    .getModuleTeachingComponent()
                    .getSubjectModule()
                    .getId()
            )
            && registration.getSemesterRegistration().getSemester().getId().equals(
                assignment.getTeachingRequirement()
                    .getTeachingGroup()
                    .getSemester()
                    .getId()
            )
            && registration.getSemesterRegistration()
                .getAcademicRegistration()
                .getAcademicYear()
                .getId()
                .equals(assignment.getTeachingRequirement()
                    .getTeachingGroup()
                    .getSemester()
                    .getAcademicYear()
                    .getId());
        boolean belongsToAudience = membershipRepository
            .existsByTeachingGroupIdAndSemesterRegistrationId(
                assignment.getTeachingRequirement().getTeachingGroup().getId(),
                registration.getSemesterRegistration().getId()
            );
        if (!sameAcademicContext || !belongsToAudience) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "The student is not registered in this teaching assignment"
            );
        }
    }

    private void validateAbsenceDate(LocalDate absenceDate) {
        if (absenceDate.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Absence date cannot be in the future"
            );
        }
    }

    private String normalizeNote(String note) {
        return note == null || note.isBlank() ? null : note.trim();
    }

    private AbsenceRecordResponse toResponse(AbsenceRecord absence) {
        ModuleRegistration registration = absence.getModuleRegistration();
        return new AbsenceRecordResponse(
            absence.getId(),
            registration.getId(),
            registration.getSemesterRegistration()
                .getAcademicRegistration()
                .getStudent()
                .getId(),
            registration.getSubjectModule().getId(),
            absence.getTeachingAssignment().getId(),
            absence.getTeachingAssignment().getProfessor().getId(),
            absence.getAbsenceDate(),
            absence.isJustified(),
            absence.getJustificationNote(),
            absence.getCreatedAt(),
            absence.getUpdatedAt()
        );
    }
}
