package com.platform.attendance.qrcheckin.application;

import com.platform.academicregistration.moduleregistration.domain.ModuleRegistrationStatus;
import com.platform.academicregistration.moduleregistration.infrastructure.ModuleRegistrationRepository;
import com.platform.attendance.qrcheckin.domain.AttendanceQrSession;
import com.platform.attendance.qrcheckin.infrastructure.AttendanceQrSessionStore;
import com.platform.attendance.qrcheckin.presentation.dto.AttendanceQrCheckInRequest;
import com.platform.attendance.qrcheckin.presentation.dto.AttendanceQrCheckInResponse;
import com.platform.attendance.qrcheckin.presentation.dto.AttendanceQrSessionResponse;
import com.platform.attendance.qrcheckin.presentation.dto.StartAttendanceQrSessionRequest;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupMembershipRepository;
import com.platform.teachingassignment.domain.TeachingAssignment;
import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import com.platform.teachingassignment.infrastructure.TeachingAssignmentRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AttendanceQrSessionService {

    private static final Duration SESSION_DURATION = Duration.ofMinutes(15);
    private static final Duration TOKEN_DURATION = Duration.ofSeconds(15);

    private final AttendanceQrSessionStore sessionStore;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final TeachingGroupMembershipRepository membershipRepository;
    private final ModuleRegistrationRepository moduleRegistrationRepository;

    public AttendanceQrSessionService(
        AttendanceQrSessionStore sessionStore,
        TeachingAssignmentRepository teachingAssignmentRepository,
        TeachingGroupMembershipRepository membershipRepository,
        ModuleRegistrationRepository moduleRegistrationRepository
    ) {
        this.sessionStore = sessionStore;
        this.teachingAssignmentRepository = teachingAssignmentRepository;
        this.membershipRepository = membershipRepository;
        this.moduleRegistrationRepository = moduleRegistrationRepository;
    }

    public AttendanceQrSessionResponse startSession(
        AuthenticatedUserPrincipal principal,
        UUID teachingAssignmentId,
        StartAttendanceQrSessionRequest request
    ) {
        TeachingAssignment assignment = requireAssignedProfessor(
            principal,
            teachingAssignmentId
        );
        if (!request.attendanceDate().equals(LocalDate.now())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "QR attendance can only be opened for today"
            );
        }

        Instant now = Instant.now();
        AttendanceQrSession session = new AttendanceQrSession(
            UUID.randomUUID(),
            assignment.getId(),
            assignment.getProfessor().getId(),
            request.attendanceDate(),
            newToken(),
            now.plus(TOKEN_DURATION),
            now.plus(SESSION_DURATION)
        );
        sessionStore.save(session, SESSION_DURATION);
        return toResponse(session);
    }

    public AttendanceQrSessionResponse getSession(
        AuthenticatedUserPrincipal principal,
        UUID sessionId
    ) {
        AttendanceQrSession session = findActiveSession(sessionId);
        requireAssignedProfessor(principal, session.teachingAssignmentId());
        return toResponse(rotateTokenIfNeeded(session));
    }

    public void closeSession(
        AuthenticatedUserPrincipal principal,
        UUID sessionId
    ) {
        AttendanceQrSession session = findActiveSession(sessionId);
        requireAssignedProfessor(principal, session.teachingAssignmentId());
        sessionStore.delete(sessionId);
    }

    public AttendanceQrCheckInResponse checkIn(
        AuthenticatedUserPrincipal principal,
        AttendanceQrCheckInRequest request
    ) {
        if (principal == null || principal.role() != AccountRoleType.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
        }

        AttendanceQrSession session = findActiveSession(request.sessionId());
        Instant now = Instant.now();
        if (session.tokenExpiresAt().isBefore(now)
            || !session.token().equals(request.token())) {
            throw new ResponseStatusException(
                HttpStatus.GONE,
                "This QR code has expired. Scan the current code"
            );
        }
        if (!rosterStudentIds(session.teachingAssignmentId())
            .contains(principal.roleEntityId())) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You do not belong to this teaching group"
            );
        }

        Duration remaining = Duration.between(now, session.closesAt());
        sessionStore.recordCheckIn(request.sessionId(), principal.roleEntityId(), remaining);
        return new AttendanceQrCheckInResponse(
            request.sessionId(),
            principal.roleEntityId(),
            now,
            "Attendance check-in recorded"
        );
    }

    private AttendanceQrSession rotateTokenIfNeeded(AttendanceQrSession session) {
        Instant now = Instant.now();
        if (session.tokenExpiresAt().isAfter(now.plusSeconds(2))) {
            return session;
        }
        AttendanceQrSession rotated = new AttendanceQrSession(
            session.id(),
            session.teachingAssignmentId(),
            session.professorId(),
            session.attendanceDate(),
            newToken(),
            now.plus(TOKEN_DURATION),
            session.closesAt()
        );
        sessionStore.save(rotated, Duration.between(now, session.closesAt()));
        return rotated;
    }

    private AttendanceQrSession findActiveSession(UUID sessionId) {
        AttendanceQrSession session = sessionStore.find(sessionId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.GONE,
                "Attendance QR session is closed or expired"
            ));
        if (!session.closesAt().isAfter(Instant.now())) {
            sessionStore.delete(sessionId);
            throw new ResponseStatusException(
                HttpStatus.GONE,
                "Attendance QR session is closed or expired"
            );
        }
        return session;
    }

    private TeachingAssignment requireAssignedProfessor(
        AuthenticatedUserPrincipal principal,
        UUID teachingAssignmentId
    ) {
        TeachingAssignment assignment = teachingAssignmentRepository
            .findById(teachingAssignmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Teaching assignment not found"
            ));
        boolean allowed = principal != null
            && principal.role() == AccountRoleType.PROFESSOR
            && principal.roleEntityId().equals(assignment.getProfessor().getId())
            && assignment.getStatus() == TeachingAssignmentStatus.ACTIVE;
        if (!allowed) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Assigned professor access required"
            );
        }
        return assignment;
    }

    private Set<UUID> rosterStudentIds(UUID teachingAssignmentId) {
        TeachingAssignment assignment = teachingAssignmentRepository
            .findById(teachingAssignmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Teaching assignment not found"
            ));
        UUID teachingGroupId = assignment.getTeachingRequirement()
            .getTeachingGroup().getId();
        UUID subjectModuleId = assignment.getTeachingRequirement()
            .getModuleTeachingComponent().getSubjectModule().getId();
        return membershipRepository.findByTeachingGroupId(teachingGroupId).stream()
            .flatMap(membership -> moduleRegistrationRepository
                .findBySemesterRegistrationIdAndStatus(
                    membership.getSemesterRegistration().getId(),
                    ModuleRegistrationStatus.ACTIVE
                )
                .stream())
            .filter(registration -> registration.getSubjectModule().getId()
                .equals(subjectModuleId))
            .map(registration -> registration.getSemesterRegistration()
                .getAcademicRegistration().getStudent().getId())
            .collect(Collectors.toSet());
    }

    private AttendanceQrSessionResponse toResponse(AttendanceQrSession session) {
        return new AttendanceQrSessionResponse(
            session.id(),
            session.teachingAssignmentId(),
            session.attendanceDate(),
            session.token(),
            session.tokenExpiresAt(),
            session.closesAt(),
            sessionStore.findCheckedInStudentIds(session.id())
        );
    }

    private String newToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
