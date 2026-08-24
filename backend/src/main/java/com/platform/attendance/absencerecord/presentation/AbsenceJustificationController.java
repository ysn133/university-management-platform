package com.platform.attendance.absencerecord.presentation;

import com.platform.attendance.absencerecord.application.AbsenceJustificationService;
import com.platform.attendance.absencerecord.presentation.dto.*;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AbsenceJustificationController {
    private final AbsenceJustificationService service;
    public AbsenceJustificationController(AbsenceJustificationService service) { this.service = service; }

    @PostMapping("/absences/{absenceId}/justifications")
    @PreAuthorize("hasRole('STUDENT')")
    public AbsenceJustificationResponse submit(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable UUID absenceId,
                                               @Valid @RequestBody SubmitAbsenceJustificationRequest request) { return service.submit(principal, absenceId, request); }

    @GetMapping("/me/absence-justifications")
    @PreAuthorize("hasRole('STUDENT')")
    public List<AbsenceJustificationResponse> getMine(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) { return service.getMine(principal); }

    @GetMapping("/teaching-assignments/{assignmentId}/absence-justifications")
    @PreAuthorize("hasRole('PROFESSOR')")
    public List<AbsenceJustificationResponse> getForAssignment(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable UUID assignmentId) { return service.getForAssignment(principal, assignmentId); }

    @PutMapping("/absence-justifications/{justificationId}/decision")
    @PreAuthorize("hasRole('PROFESSOR')")
    public AbsenceJustificationResponse review(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable UUID justificationId,
                                               @Valid @RequestBody ReviewAbsenceJustificationRequest request) { return service.review(principal, justificationId, request); }

    @GetMapping("/absence-justifications/{justificationId}/document")
    @PreAuthorize("hasAnyRole('STUDENT', 'PROFESSOR')")
    public ResponseEntity<byte[]> download(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable UUID justificationId) {
        var download = service.download(principal, justificationId);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(download.contentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(download.filename()).build().toString())
            .body(download.content());
    }
}
