package com.platform.universitygovernance.academicdomain.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.academicdomain.application.AcademicDomainService;
import com.platform.universitygovernance.academicdomain.presentation.dto.AcademicDomainResponse;
import com.platform.universitygovernance.academicdomain.presentation.dto.CreateAcademicDomainRequest;
import com.platform.universitygovernance.academicdomain.presentation.dto.UpdateAcademicDomainRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class AcademicDomainController {

    private final AcademicDomainService academicDomainService;

    public AcademicDomainController(AcademicDomainService academicDomainService) {
        this.academicDomainService = academicDomainService;
    }

    @PostMapping("/establishments/{establishmentId}/academic-domains")
    public AcademicDomainResponse createAcademicDomain(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId,
        @Valid @RequestBody CreateAcademicDomainRequest request
    ) {
        return academicDomainService.createAcademicDomain(principal, establishmentId, request);
    }

    @GetMapping("/establishments/{establishmentId}/academic-domains")
    public List<AcademicDomainResponse> getAcademicDomains(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId
    ) {
        return academicDomainService.getAcademicDomains(principal, establishmentId);
    }

    @GetMapping("/academic-domains/{academicDomainId}")
    public AcademicDomainResponse getAcademicDomain(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicDomainId
    ) {
        return academicDomainService.getAcademicDomain(principal, academicDomainId);
    }

    @PutMapping("/academic-domains/{academicDomainId}")
    public AcademicDomainResponse updateAcademicDomain(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicDomainId,
        @Valid @RequestBody UpdateAcademicDomainRequest request
    ) {
        return academicDomainService.updateAcademicDomain(principal, academicDomainId, request);
    }

    @DeleteMapping("/academic-domains/{academicDomainId}")
    public ActionResponse deleteAcademicDomain(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicDomainId
    ) {
        return academicDomainService.deleteAcademicDomain(principal, academicDomainId);
    }
}
