package com.platform.universitygovernance.academicyear.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.academicyear.application.AcademicYearService;
import com.platform.universitygovernance.academicyear.presentation.dto.AcademicYearResponse;
import com.platform.universitygovernance.academicyear.presentation.dto.CreateAcademicYearRequest;
import com.platform.universitygovernance.academicyear.presentation.dto.UpdateAcademicYearRequest;
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
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    public AcademicYearController(AcademicYearService academicYearService) {
        this.academicYearService = academicYearService;
    }

    @PostMapping("/establishments/{establishmentId}/academic-years")
    public AcademicYearResponse createAcademicYear(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId,
        @Valid @RequestBody CreateAcademicYearRequest request
    ) {
        return academicYearService.createAcademicYear(principal, establishmentId, request);
    }

    @GetMapping("/establishments/{establishmentId}/academic-years")
    public List<AcademicYearResponse> getAcademicYears(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId
    ) {
        return academicYearService.getAcademicYears(principal, establishmentId);
    }

    @GetMapping("/academic-years/{academicYearId}")
    public AcademicYearResponse getAcademicYear(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicYearId
    ) {
        return academicYearService.getAcademicYear(principal, academicYearId);
    }

    @PutMapping("/academic-years/{academicYearId}")
    public AcademicYearResponse updateAcademicYear(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicYearId,
        @Valid @RequestBody UpdateAcademicYearRequest request
    ) {
        return academicYearService.updateAcademicYear(principal, academicYearId, request);
    }

    @DeleteMapping("/academic-years/{academicYearId}")
    public ActionResponse deleteAcademicYear(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicYearId
    ) {
        return academicYearService.deleteAcademicYear(principal, academicYearId);
    }
}
