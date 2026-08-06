package com.platform.scheduling.teachinggroup.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.teachinggroup.application.TeachingGroupManagementService;
import com.platform.scheduling.teachinggroup.presentation.dto.TeachingGroupRosterResponse;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
public class TeachingGroupController {

    private final TeachingGroupManagementService managementService;

    public TeachingGroupController(TeachingGroupManagementService managementService) {
        this.managementService = managementService;
    }

    @GetMapping("/semesters/{semesterId}/teaching-groups")
    public TeachingGroupRosterResponse getRoster(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID semesterId
    ) {
        return managementService.getRoster(principal, semesterId);
    }

    @PostMapping("/semesters/{semesterId}/teaching-groups/generate")
    public TeachingGroupRosterResponse generate(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID semesterId
    ) {
        return managementService.generate(principal, semesterId);
    }

    @PutMapping("/teaching-groups/{teachingGroupId}/members/{semesterRegistrationId}")
    public TeachingGroupRosterResponse moveMember(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID teachingGroupId,
        @PathVariable UUID semesterRegistrationId
    ) {
        return managementService.moveMember(
            principal,
            teachingGroupId,
            semesterRegistrationId
        );
    }
}
