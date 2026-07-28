package com.platform.universitygovernance.academicruleprofile.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.academicruleprofile.application.AcademicRuleProfileService;
import com.platform.universitygovernance.academicruleprofile.presentation.dto.AcademicRuleProfileResponse;
import com.platform.universitygovernance.academicruleprofile.presentation.dto.CreateAcademicRuleProfileRequest;
import com.platform.universitygovernance.academicruleprofile.presentation.dto.UpdateAcademicRuleProfileRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
public class AcademicRuleProfileController {

    private final AcademicRuleProfileService academicRuleProfileService;

    public AcademicRuleProfileController(AcademicRuleProfileService academicRuleProfileService) {
        this.academicRuleProfileService = academicRuleProfileService;
    }

    @PostMapping("/establishments/{establishmentId}/academic-rule-profiles")
    public AcademicRuleProfileResponse createAcademicRuleProfile(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId,
        @Valid @RequestBody CreateAcademicRuleProfileRequest request
    ) {
        return academicRuleProfileService.createAcademicRuleProfile(
            principal,
            establishmentId,
            request
        );
    }

    @GetMapping("/establishments/{establishmentId}/academic-rule-profiles")
    public List<AcademicRuleProfileResponse> getAcademicRuleProfiles(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId
    ) {
        return academicRuleProfileService.getAcademicRuleProfiles(principal, establishmentId);
    }

    @GetMapping("/academic-rule-profiles/{academicRuleProfileId}")
    public AcademicRuleProfileResponse getAcademicRuleProfile(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicRuleProfileId
    ) {
        return academicRuleProfileService.getAcademicRuleProfile(principal, academicRuleProfileId);
    }

    @PutMapping("/academic-rule-profiles/{academicRuleProfileId}")
    public AcademicRuleProfileResponse updateAcademicRuleProfile(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicRuleProfileId,
        @Valid @RequestBody UpdateAcademicRuleProfileRequest request
    ) {
        return academicRuleProfileService.updateAcademicRuleProfile(
            principal,
            academicRuleProfileId,
            request
        );
    }
}
