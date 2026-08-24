package com.platform.attendance.absencerecord.application;

import com.platform.academicregistration.moduleregistration.domain.ModuleRegistration;
import com.platform.academicregistration.moduleregistration.domain.ModuleRegistrationStatus;
import com.platform.academicregistration.moduleregistration.infrastructure.ModuleRegistrationRepository;
import com.platform.attendance.absencerecord.domain.AbsenceRecord;
import com.platform.attendance.absencerecord.infrastructure.AbsenceRecordRepository;
import com.platform.attendance.absencerecord.presentation.dto.AbsenceRecordResponse;
import com.platform.attendance.absencerecord.presentation.dto.CreateAbsenceRequest;
import com.platform.attendance.absencerecord.presentation.dto.ConfirmAttendanceRequest;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupMembershipRepository;
import com.platform.teachingassignment.domain.TeachingAssignment;
import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import com.platform.teachingassignment.infrastructure.TeachingAssignmentRepository;
import com.platform.teachingassignment.application.TeachingAssignmentRosterService;
import java.time.LocalDate;
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
public class AbsenceRecordService {

    private final AbsenceRecordRepository absenceRecordRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final ModuleRegistrationRepository moduleRegistrationRepository;
    private final TeachingGroupMembershipRepository membershipRepository;
    private final TeachingAssignmentRosterService rosterService;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public AbsenceRecordService(
        AbsenceRecordRepository absenceRecordRepository,
        TeachingAssignmentRepository teachingAssignmentRepository,
        ModuleRegistrationRepository moduleRegistrationRepository,
        TeachingGroupMembershipRepository membershipRepository,
        TeachingAssignmentRosterService rosterService,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.absenceRecordRepository = absenceRecordRepository;
        this.teachingAssignmentRepository = teachingAssignmentRepository;
        this.moduleRegistrationRepository = moduleRegistrationRepository;
        this.membershipRepository = membershipRepository;
        this.rosterService = rosterService;
        this.permissionAuthorizationService = permissionAuthorizationService;
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

    @Transactional
    public List<AbsenceRecordResponse> confirmAttendance(
        AuthenticatedUserPrincipal principal,
        UUID teachingAssignmentId,
        ConfirmAttendanceRequest request
    ) {
        TeachingAssignment assignment = findAssignedTeachingAssignment(
            principal,
            teachingAssignmentId
        );
        validateAbsenceDate(request.attendanceDate());

        Map<UUID, ModuleRegistration> rosterByStudentId = attendanceRoster(assignment)
            .stream()
            .collect(Collectors.toMap(
                registration -> registration.getSemesterRegistration()
                    .getAcademicRegistration().getStudent().getId(),
                Function.identity(),
                (first, ignored) -> first
            ));
        Set<UUID> absentStudentIds = request.absentStudentIds();
        if (!rosterByStudentId.keySet().containsAll(absentStudentIds)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "An absent student does not belong to this teaching assignment"
            );
        }

        List<AbsenceRecord> existing = absenceRecordRepository
            .findByTeachingAssignmentIdAndAbsenceDateOrderByCreatedAtAsc(
                teachingAssignmentId,
                request.attendanceDate()
            );
        Map<UUID, AbsenceRecord> existingByStudentId = existing.stream()
            .collect(Collectors.toMap(
                absence -> absence.getModuleRegistration().getSemesterRegistration()
                    .getAcademicRegistration().getStudent().getId(),
                Function.identity(),
                (first, ignored) -> first
            ));

        List<AbsenceRecord> removed = existing.stream()
            .filter(absence -> !absentStudentIds.contains(
                absence.getModuleRegistration().getSemesterRegistration()
                    .getAcademicRegistration().getStudent().getId()
            ))
            .toList();
        absenceRecordRepository.deleteAll(removed);

        List<AbsenceRecord> created = absentStudentIds.stream()
            .filter(studentId -> !existingByStudentId.containsKey(studentId))
            .map(studentId -> newAbsence(
                rosterByStudentId.get(studentId),
                assignment,
                request.attendanceDate()
            ))
            .toList();
        absenceRecordRepository.saveAll(created);

        return absenceRecordRepository
            .findByTeachingAssignmentIdAndAbsenceDateOrderByCreatedAtAsc(
                teachingAssignmentId,
                request.attendanceDate()
            )
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

    @Transactional(readOnly = true)
    public List<AbsenceRecordResponse> getEstablishmentAbsences(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        UUID studentId,
        UUID academicYearId,
        UUID semesterId,
        UUID subjectModuleId,
        Boolean justified
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.ABSENCE_VIEW
        );
        return absenceRecordRepository
            .findByModuleRegistrationSemesterRegistrationAcademicRegistrationStudentEstablishmentIdOrderByAbsenceDateDesc(
                establishmentId
            )
            .stream()
            .filter(absence -> studentId == null || studentId.equals(
                absence.getModuleRegistration().getSemesterRegistration()
                    .getAcademicRegistration().getStudent().getId()
            ))
            .filter(absence -> academicYearId == null || academicYearId.equals(
                absence.getModuleRegistration().getSemesterRegistration()
                    .getAcademicRegistration().getAcademicYear().getId()
            ))
            .filter(absence -> semesterId == null || semesterId.equals(
                absence.getModuleRegistration().getSemesterRegistration().getSemester().getId()
            ))
            .filter(absence -> subjectModuleId == null || subjectModuleId.equals(
                absence.getModuleRegistration().getSubjectModule().getId()
            ))
            .filter(absence -> justified == null || justified == absence.isJustified())
            .map(this::toResponse)
            .toList();
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

    private List<ModuleRegistration> attendanceRoster(TeachingAssignment assignment) {
        return rosterService.activeModuleRegistrations(assignment);
    }

    private AbsenceRecord newAbsence(
        ModuleRegistration registration,
        TeachingAssignment assignment,
        LocalDate absenceDate
    ) {
        AbsenceRecord absence = new AbsenceRecord();
        absence.setModuleRegistration(registration);
        absence.setTeachingAssignment(assignment);
        absence.setAbsenceDate(absenceDate);
        absence.setJustified(false);
        return absence;
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
            registration.getSubjectModule().getCode(),
            registration.getSubjectModule().getTitle(),
            registration.getSemesterRegistration().getSemester().getAcademicYear().getId(),
            registration.getSemesterRegistration().getSemester().getAcademicYear().getLabel(),
            registration.getSemesterRegistration().getSemester().getId(),
            registration.getSemesterRegistration().getSemester().getName(),
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
