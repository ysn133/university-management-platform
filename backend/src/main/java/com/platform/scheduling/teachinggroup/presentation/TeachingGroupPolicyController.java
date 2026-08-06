package com.platform.scheduling.teachinggroup.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.teachinggroup.application.TeachingGroupPolicyService;
import com.platform.scheduling.teachinggroup.presentation.dto.ReplaceTeachingGroupPoliciesRequest;
import com.platform.scheduling.teachinggroup.presentation.dto.TeachingGroupPolicyResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/academic-levels/{academicLevelId}/teaching-group-policies")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
public class TeachingGroupPolicyController {

    private final TeachingGroupPolicyService policyService;

    public TeachingGroupPolicyController(TeachingGroupPolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping
    public List<TeachingGroupPolicyResponse> getPolicies(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicLevelId,
        @RequestParam UUID academicYearId
    ) {
        return policyService.getPolicies(principal, academicLevelId, academicYearId);
    }

    @PutMapping
    public List<TeachingGroupPolicyResponse> replacePolicies(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicLevelId,
        @RequestParam UUID academicYearId,
        @Valid @RequestBody ReplaceTeachingGroupPoliciesRequest request
    ) {
        return policyService.replacePolicies(
            principal,
            academicLevelId,
            academicYearId,
            request
        );
    }
}
