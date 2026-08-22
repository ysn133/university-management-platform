package com.platform.assessment.progressiondecision.presentation;

import com.platform.assessment.graduationdecision.application.GraduationDecisionService;
import com.platform.assessment.graduationdecision.presentation.dto.GraduationDecisionResponse;
import com.platform.assessment.progressiondecision.application.ManagedProgressionDecisionService;
import com.platform.assessment.progressiondecision.presentation.dto.ManagedProgressionDecisionResponse;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@PreAuthorize("hasRole('STUDENT')")
public class StudentAcademicDecisionController {

    private final ManagedProgressionDecisionService progressionService;
    private final GraduationDecisionService graduationService;

    public StudentAcademicDecisionController(
        ManagedProgressionDecisionService progressionService,
        GraduationDecisionService graduationService
    ) {
        this.progressionService = progressionService;
        this.graduationService = graduationService;
    }

    @GetMapping("/progression-decisions")
    public List<ManagedProgressionDecisionResponse> progression(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return progressionService.getMine(principal);
    }

    @GetMapping("/graduation-decisions")
    public List<GraduationDecisionResponse> graduation(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return graduationService.getMine(principal);
    }
}
