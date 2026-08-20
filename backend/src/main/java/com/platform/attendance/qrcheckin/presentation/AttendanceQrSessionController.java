package com.platform.attendance.qrcheckin.presentation;

import com.platform.attendance.qrcheckin.application.AttendanceQrSessionService;
import com.platform.attendance.qrcheckin.presentation.dto.AttendanceQrCheckInRequest;
import com.platform.attendance.qrcheckin.presentation.dto.AttendanceQrCheckInResponse;
import com.platform.attendance.qrcheckin.presentation.dto.AttendanceQrSessionResponse;
import com.platform.attendance.qrcheckin.presentation.dto.StartAttendanceQrSessionRequest;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attendance/qr-sessions")
public class AttendanceQrSessionController {

    private final AttendanceQrSessionService sessionService;

    public AttendanceQrSessionController(AttendanceQrSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/teaching-assignments/{teachingAssignmentId}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public AttendanceQrSessionResponse startSession(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID teachingAssignmentId,
        @Valid @RequestBody StartAttendanceQrSessionRequest request
    ) {
        return sessionService.startSession(principal, teachingAssignmentId, request);
    }

    @GetMapping("/{sessionId}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public AttendanceQrSessionResponse getSession(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID sessionId
    ) {
        return sessionService.getSession(principal, sessionId);
    }

    @DeleteMapping("/{sessionId}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Void> closeSession(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID sessionId
    ) {
        sessionService.closeSession(principal, sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('STUDENT')")
    public AttendanceQrCheckInResponse checkIn(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @Valid @RequestBody AttendanceQrCheckInRequest request
    ) {
        return sessionService.checkIn(principal, request);
    }
}
