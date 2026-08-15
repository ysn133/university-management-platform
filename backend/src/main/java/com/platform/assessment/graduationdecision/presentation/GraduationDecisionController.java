package com.platform.assessment.graduationdecision.presentation;

import com.platform.assessment.graduationdecision.application.GraduationDecisionService;
import com.platform.assessment.graduationdecision.presentation.dto.GraduationDecisionResponse;
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
@RequestMapping("/api/v1/academic-levels/{academicLevelId}/academic-years/{academicYearId}/graduation-decisions")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
public class GraduationDecisionController {

    private final GraduationDecisionService service;

    public GraduationDecisionController(GraduationDecisionService service) {
        this.service = service;
    }

    @GetMapping
    public List<GraduationDecisionResponse> get(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicLevelId,
        @PathVariable UUID academicYearId
    ) {
        return service.get(principal, academicLevelId, academicYearId);
    }

    @PostMapping("/generate")
    public List<GraduationDecisionResponse> generate(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicLevelId,
        @PathVariable UUID academicYearId
    ) {
        return service.generate(principal, academicLevelId, academicYearId);
    }
}
