package com.platform.teachingassignment.rankpreference.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.teachingassignment.rankpreference.application.TeachingAssignmentRankPreferenceService;
import com.platform.teachingassignment.rankpreference.presentation.dto.ReplaceRankPreferencesRequest;
import com.platform.teachingassignment.rankpreference.presentation.dto.TeachingAssignmentRankPreferenceResponse;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/establishments/{establishmentId}/teaching-assignment-rank-preferences")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
public class TeachingAssignmentRankPreferenceController {
    private final TeachingAssignmentRankPreferenceService service;
    public TeachingAssignmentRankPreferenceController(TeachingAssignmentRankPreferenceService service) { this.service = service; }

    @GetMapping
    public List<TeachingAssignmentRankPreferenceResponse> list(@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId) { return service.list(principal, establishmentId); }

    @PutMapping("/{componentType}")
    public List<TeachingAssignmentRankPreferenceResponse> replace(@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId, @PathVariable TeachingComponentType componentType,
        @Valid @RequestBody ReplaceRankPreferencesRequest request) {
        return service.replace(principal, establishmentId, componentType, request);
    }
}
