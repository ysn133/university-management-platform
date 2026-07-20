package com.platform.academicregistration.registration.presentation;

import com.platform.academicregistration.registration.application.AcademicRegistrationService;
import com.platform.academicregistration.registration.presentation.dto.AcademicRegistrationResponse;
import com.platform.academicregistration.registration.presentation.dto.CreateAcademicRegistrationRequest;
import com.platform.academicregistration.registration.presentation.dto.UpdateAcademicRegistrationRequest;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
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
public class AcademicRegistrationController {

    private final AcademicRegistrationService academicRegistrationService;

    public AcademicRegistrationController(
        AcademicRegistrationService academicRegistrationService
    ) {
        this.academicRegistrationService = academicRegistrationService;
    }

    @PostMapping("/establishments/{establishmentId}/academic-registrations")
    public AcademicRegistrationResponse createAcademicRegistration(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId,
        @Valid @RequestBody CreateAcademicRegistrationRequest request
    ) {
        return academicRegistrationService.createAcademicRegistration(
            principal,
            establishmentId,
            request
        );
    }

    @GetMapping("/establishments/{establishmentId}/academic-registrations")
    public List<AcademicRegistrationResponse> getAcademicRegistrations(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId
    ) {
        return academicRegistrationService.getAcademicRegistrations(principal, establishmentId);
    }

    @GetMapping("/students/{studentId}/academic-registrations")
    public List<AcademicRegistrationResponse> getStudentAcademicRegistrations(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID studentId
    ) {
        return academicRegistrationService.getStudentAcademicRegistrations(principal, studentId);
    }

    @GetMapping("/academic-registrations/{academicRegistrationId}")
    public AcademicRegistrationResponse getAcademicRegistration(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicRegistrationId
    ) {
        return academicRegistrationService.getAcademicRegistration(
            principal,
            academicRegistrationId
        );
    }

    @PutMapping("/academic-registrations/{academicRegistrationId}")
    public AcademicRegistrationResponse updateAcademicRegistration(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicRegistrationId,
        @Valid @RequestBody UpdateAcademicRegistrationRequest request
    ) {
        return academicRegistrationService.updateAcademicRegistration(
            principal,
            academicRegistrationId,
            request
        );
    }
}
