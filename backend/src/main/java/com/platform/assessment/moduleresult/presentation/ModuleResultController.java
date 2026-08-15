package com.platform.assessment.moduleresult.presentation;

import com.platform.assessment.moduleresult.application.FinalResultService;
import com.platform.assessment.moduleresult.presentation.dto.FinalResultResponse;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/semesters/{semesterId}/class-groups/{classGroupId}/final-results")
public class ModuleResultController {

    private final FinalResultService finalResultService;

    public ModuleResultController(FinalResultService finalResultService) {
        this.finalResultService = finalResultService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'PROFESSOR')")
    public List<FinalResultResponse> get(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID semesterId,
        @PathVariable UUID classGroupId,
        @RequestParam(required = false) UUID subjectModuleId
    ) {
        return finalResultService.get(principal, semesterId, classGroupId, subjectModuleId);
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    public List<FinalResultResponse> generate(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID semesterId,
        @PathVariable UUID classGroupId
    ) {
        return finalResultService.generate(principal, semesterId, classGroupId);
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    public void clear(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID semesterId,
        @PathVariable UUID classGroupId
    ) {
        finalResultService.clear(principal, semesterId, classGroupId);
    }
}
