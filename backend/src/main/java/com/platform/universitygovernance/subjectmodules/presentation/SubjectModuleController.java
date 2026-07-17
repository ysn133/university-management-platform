package com.platform.universitygovernance.subjectmodules.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.subjectmodules.application.SubjectModuleService;
import com.platform.universitygovernance.subjectmodules.presentation.dto.CreateSubjectModuleRequest;
import com.platform.universitygovernance.subjectmodules.presentation.dto.SubjectModuleResponse;
import com.platform.universitygovernance.subjectmodules.presentation.dto.UpdateSubjectModuleRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class SubjectModuleController {

    private final SubjectModuleService subjectModuleService;

    public SubjectModuleController(SubjectModuleService subjectModuleService) {
        this.subjectModuleService = subjectModuleService;
    }

    @PostMapping("/semesters/{semesterId}/subject-modules")
    public SubjectModuleResponse createSubjectModule(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID semesterId,
        @Valid @RequestBody CreateSubjectModuleRequest request
    ) {
        return subjectModuleService.createSubjectModule(principal, semesterId, request);
    }

    @GetMapping("/semesters/{semesterId}/subject-modules")
    public List<SubjectModuleResponse> getSubjectModules(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID semesterId
    ) {
        return subjectModuleService.getSubjectModules(principal, semesterId);
    }

    @GetMapping("/subject-modules/{subjectModuleId}")
    public SubjectModuleResponse getSubjectModule(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID subjectModuleId
    ) {
        return subjectModuleService.getSubjectModule(principal, subjectModuleId);
    }

    @PutMapping("/subject-modules/{subjectModuleId}")
    public SubjectModuleResponse updateSubjectModule(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID subjectModuleId,
        @Valid @RequestBody UpdateSubjectModuleRequest request
    ) {
        return subjectModuleService.updateSubjectModule(principal, subjectModuleId, request);
    }

    @DeleteMapping("/subject-modules/{subjectModuleId}")
    public ActionResponse deleteSubjectModule(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID subjectModuleId
    ) {
        return subjectModuleService.deleteSubjectModule(principal, subjectModuleId);
    }
}
