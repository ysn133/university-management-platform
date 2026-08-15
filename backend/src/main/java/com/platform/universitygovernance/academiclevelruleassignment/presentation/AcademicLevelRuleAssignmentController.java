package com.platform.universitygovernance.academiclevelruleassignment.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.academiclevelruleassignment.application.AcademicLevelRuleAssignmentService;
import com.platform.universitygovernance.academiclevelruleassignment.presentation.dto.AcademicLevelRuleAssignmentResponse;
import com.platform.universitygovernance.academiclevelruleassignment.presentation.dto.CreateAcademicLevelRuleAssignmentRequest;
import com.platform.universitygovernance.academiclevelruleassignment.presentation.dto.UpdateAcademicLevelRuleAssignmentRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
public class AcademicLevelRuleAssignmentController {

    private final AcademicLevelRuleAssignmentService assignmentService;

    public AcademicLevelRuleAssignmentController(
        AcademicLevelRuleAssignmentService assignmentService
    ) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/academic-levels/{academicLevelId}/rule-assignments")
    public AcademicLevelRuleAssignmentResponse createAssignment(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicLevelId,
        @Valid @RequestBody CreateAcademicLevelRuleAssignmentRequest request
    ) {
        return assignmentService.createAssignment(principal, academicLevelId, request);
    }

    @GetMapping("/academic-levels/{academicLevelId}/rule-assignments")
    public List<AcademicLevelRuleAssignmentResponse> getAssignments(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicLevelId
    ) {
        return assignmentService.getAssignments(principal, academicLevelId);
    }

    @GetMapping("/academic-level-rule-assignments/{assignmentId}")
    public AcademicLevelRuleAssignmentResponse getAssignment(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID assignmentId
    ) {
        return assignmentService.getAssignment(principal, assignmentId);
    }

    @PutMapping("/academic-level-rule-assignments/{assignmentId}")
    public AcademicLevelRuleAssignmentResponse updateAssignment(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID assignmentId,
        @Valid @RequestBody UpdateAcademicLevelRuleAssignmentRequest request
    ) {
        return assignmentService.updateAssignment(principal, assignmentId, request);
    }
}
