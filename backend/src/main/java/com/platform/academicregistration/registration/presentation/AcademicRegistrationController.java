package com.platform.academicregistration.registration.presentation;

import com.platform.academicregistration.registration.application.AcademicRegistrationService;
import com.platform.academicregistration.registration.presentation.dto.AcademicRegistrationResponse;
import com.platform.academicregistration.registration.presentation.dto.CreateAcademicRegistrationRequest;
import com.platform.academicregistration.registration.presentation.dto.UpdateAcademicRegistrationRequest;
import com.platform.academicregistration.registration.domain.AcademicRegistrationStatus;
import com.platform.academicregistration.semesterregistration.presentation.dto.SemesterRegistrationResponse;
import com.platform.academicregistration.moduleregistration.presentation.dto.ModuleRegistrationResponse;
import com.platform.assessment.progressiondecision.presentation.dto.ProgressionDecisionResponse;
import com.platform.assessment.semesterresult.presentation.dto.SemesterResultResponse;
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
import org.springframework.web.bind.annotation.RequestParam;

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
        @PathVariable UUID establishmentId,
        @RequestParam(required = false) UUID academicYearId,
        @RequestParam(required = false) UUID programFiliereId,
        @RequestParam(required = false) UUID academicLevelId,
        @RequestParam(required = false) UUID semesterId,
        @RequestParam(required = false) UUID classGroupId,
        @RequestParam(required = false) AcademicRegistrationStatus status,
        @RequestParam(required = false) String query
    ) {
        return academicRegistrationService.getAcademicRegistrations(
            principal,
            establishmentId,
            academicYearId,
            programFiliereId,
            academicLevelId,
            semesterId,
            classGroupId,
            status,
            query
        );
    }

    @GetMapping("/academic-registrations/{academicRegistrationId}/semester-registrations")
    public List<SemesterRegistrationResponse> getSemesterRegistrations(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicRegistrationId
    ) {
        return academicRegistrationService.getSemesterRegistrations(
            principal,
            academicRegistrationId
        );
    }

    @GetMapping("/semester-registrations/{semesterRegistrationId}/module-registrations")
    public List<ModuleRegistrationResponse> getModuleRegistrations(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID semesterRegistrationId
    ) {
        return academicRegistrationService.getModuleRegistrations(
            principal,
            semesterRegistrationId
        );
    }

    @GetMapping("/semester-registrations/{semesterRegistrationId}/result")
    public SemesterResultResponse getSemesterResult(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID semesterRegistrationId
    ) {
        return academicRegistrationService.getSemesterResult(principal, semesterRegistrationId);
    }

    @GetMapping("/academic-registrations/{academicRegistrationId}/progression-decision")
    public ProgressionDecisionResponse getProgressionDecision(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicRegistrationId
    ) {
        return academicRegistrationService.getProgressionDecision(
            principal,
            academicRegistrationId
        );
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
