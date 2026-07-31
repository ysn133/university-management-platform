package com.platform.teachingrequirement.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.teachingrequirement.application.TeachingRequirementService;
import com.platform.teachingrequirement.presentation.dto.TeachingRequirementResponse;
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
@RequestMapping("/api/v1/semesters/{semesterId}/teaching-requirements")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
public class TeachingRequirementController {

    private final TeachingRequirementService teachingRequirementService;

    public TeachingRequirementController(
        TeachingRequirementService teachingRequirementService
    ) {
        this.teachingRequirementService = teachingRequirementService;
    }

    @PostMapping("/generate")
    public List<TeachingRequirementResponse> generate(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID semesterId
    ) {
        return teachingRequirementService.generateForSemester(principal, semesterId);
    }

    @GetMapping
    public List<TeachingRequirementResponse> getForSemester(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID semesterId
    ) {
        return teachingRequirementService.getForSemester(principal, semesterId);
    }
}
