package com.platform.teachingassignment.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.teachingassignment.application.TeachingAssignmentService;
import com.platform.teachingassignment.presentation.dto.CreateTeachingAssignmentRequest;
import com.platform.teachingassignment.presentation.dto.TeachingAssignmentResponse;
import com.platform.teachingassignment.presentation.dto.TeachingAssignmentStudentResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
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
@RequestMapping("/api/v1")
public class TeachingAssignmentController {

    private final TeachingAssignmentService teachingAssignmentService;

    public TeachingAssignmentController(
        TeachingAssignmentService teachingAssignmentService
    ) {
        this.teachingAssignmentService = teachingAssignmentService;
    }

    @PostMapping("/establishments/{establishmentId}/teaching-assignments")
    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    public TeachingAssignmentResponse createTeachingAssignment(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId,
        @Valid @RequestBody CreateTeachingAssignmentRequest request
    ) {
        return teachingAssignmentService.createTeachingAssignment(
            principal,
            establishmentId,
            request
        );
    }

    @GetMapping("/establishments/{establishmentId}/teaching-assignments")
    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    public List<TeachingAssignmentResponse> getTeachingAssignments(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId
    ) {
        return teachingAssignmentService.getTeachingAssignments(principal, establishmentId);
    }

    @GetMapping("/teaching-assignments/{teachingAssignmentId}")
    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'PROFESSOR')")
    public TeachingAssignmentResponse getTeachingAssignment(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID teachingAssignmentId
    ) {
        return teachingAssignmentService.getTeachingAssignment(principal, teachingAssignmentId);
    }

    @GetMapping("/me/teaching-assignments")
    @PreAuthorize("hasRole('PROFESSOR')")
    public List<TeachingAssignmentResponse> getMyTeachingAssignments(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return teachingAssignmentService.getMyTeachingAssignments(principal);
    }

    @GetMapping("/teaching-assignments/{teachingAssignmentId}/students")
    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'PROFESSOR')")
    public List<TeachingAssignmentStudentResponse> getTeachingAssignmentStudents(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID teachingAssignmentId
    ) {
        return teachingAssignmentService.getTeachingAssignmentStudents(
            principal,
            teachingAssignmentId
        );
    }

    @DeleteMapping("/teaching-assignments/{teachingAssignmentId}")
    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    public ActionResponse unassignProfessor(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID teachingAssignmentId
    ) {
        return teachingAssignmentService.unassignProfessor(principal, teachingAssignmentId);
    }
}
