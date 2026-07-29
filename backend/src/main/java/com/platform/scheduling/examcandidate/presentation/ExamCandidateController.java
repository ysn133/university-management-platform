package com.platform.scheduling.examcandidate.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.examcandidate.application.ExamCandidateService;
import com.platform.scheduling.examcandidate.presentation.dto.ExamCandidateResponse;
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
@RequestMapping("/api/v1")
public class ExamCandidateController {

    private final ExamCandidateService examCandidateService;

    public ExamCandidateController(ExamCandidateService examCandidateService) {
        this.examCandidateService = examCandidateService;
    }

    @PostMapping("/module-exams/{moduleExamId}/candidates/generate")
    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    public List<ExamCandidateResponse> generateCandidates(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID moduleExamId
    ) {
        return examCandidateService.generateCandidates(principal, moduleExamId);
    }

    @GetMapping("/module-exams/{moduleExamId}/candidates")
    @PreAuthorize(
        "hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'PROFESSOR')"
    )
    public List<ExamCandidateResponse> getCandidates(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID moduleExamId
    ) {
        return examCandidateService.getCandidates(principal, moduleExamId);
    }

    @GetMapping("/me/exam-invitations")
    @PreAuthorize("hasRole('STUDENT')")
    public List<ExamCandidateResponse> getMyInvitations(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return examCandidateService.getMyInvitations(principal);
    }
}
