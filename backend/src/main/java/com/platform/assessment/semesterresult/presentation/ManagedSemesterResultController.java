package com.platform.assessment.semesterresult.presentation;

import com.platform.assessment.semesterresult.application.ManagedSemesterResultService;
import com.platform.assessment.semesterresult.presentation.dto.ManagedSemesterResultResponse;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/semesters/{semesterId}/class-groups/{classGroupId}/semester-results")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
public class ManagedSemesterResultController {

    private final ManagedSemesterResultService service;

    public ManagedSemesterResultController(ManagedSemesterResultService service) {
        this.service = service;
    }

    @GetMapping
    public List<ManagedSemesterResultResponse> get(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID semesterId,
        @PathVariable UUID classGroupId
    ) {
        return service.get(principal, semesterId, classGroupId);
    }

    @PostMapping("/generate")
    public List<ManagedSemesterResultResponse> generate(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID semesterId,
        @PathVariable UUID classGroupId
    ) {
        return service.generate(principal, semesterId, classGroupId);
    }
}
