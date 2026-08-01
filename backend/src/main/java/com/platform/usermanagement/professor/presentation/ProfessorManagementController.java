package com.platform.usermanagement.professor.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.usermanagement.professor.application.ProfessorManagementService;
import com.platform.usermanagement.professor.presentation.dto.CreateProfessorRequest;
import com.platform.usermanagement.professor.presentation.dto.CreateProfessorResponse;
import com.platform.usermanagement.professor.presentation.dto.ProfessorProfileResponse;
import com.platform.usermanagement.professor.presentation.dto.UpdateProfessorRequest;
import com.platform.usermanagement.shared.presentation.dto.ResetManagedPasswordRequest;
import com.platform.shared.presentation.ActionResponse;
import com.platform.identityaccess.domain.AccountStatus;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
public class ProfessorManagementController {

    private final ProfessorManagementService professorManagementService;

    public ProfessorManagementController(
        ProfessorManagementService professorManagementService
    ) {
        this.professorManagementService = professorManagementService;
    }

    @PostMapping("/establishments/{establishmentId}/professors")
    public CreateProfessorResponse createProfessor(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId,
        @Valid @RequestBody CreateProfessorRequest request
    ) {
        return professorManagementService.createProfessor(
            principal,
            establishmentId,
            request
        );
    }

    @GetMapping("/establishments/{establishmentId}/professors")
    public List<ProfessorProfileResponse> getProfessors(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId,
        @RequestParam(required = false) String query,
        @RequestParam(required = false) AccountStatus status,
        @RequestParam(required = false) LocalDate joinedFrom,
        @RequestParam(required = false) LocalDate joinedTo,
        @RequestParam(required = false) UUID academicDomainId
    ) {
        return professorManagementService.getProfessors(
            principal, establishmentId, query, status, joinedFrom, joinedTo, academicDomainId
        );
    }

    @GetMapping("/professors/{professorId}")
    public ProfessorProfileResponse getProfessor(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID professorId
    ) {
        return professorManagementService.getProfessor(principal, professorId);
    }

    @PutMapping("/professors/{professorId}")
    public ProfessorProfileResponse update(@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID professorId, @Valid @RequestBody UpdateProfessorRequest request) {
        return professorManagementService.updateProfessor(principal, professorId, request);
    }

    @PostMapping("/professors/{professorId}/password-reset")
    public ActionResponse resetPassword(@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID professorId, @Valid @RequestBody ResetManagedPasswordRequest request) {
        return professorManagementService.resetPassword(principal, professorId, request);
    }

    @PostMapping("/professors/{professorId}/lock")
    public ActionResponse lock(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable UUID professorId) {
        return professorManagementService.lockAccount(principal, professorId);
    }

    @PostMapping("/professors/{professorId}/unlock")
    public ActionResponse unlock(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable UUID professorId) {
        return professorManagementService.unlockAccount(principal, professorId);
    }

    @PostMapping("/professors/{professorId}/deactivate")
    public ActionResponse deactivate(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable UUID professorId) {
        return professorManagementService.deactivateAccount(principal, professorId);
    }

    @PostMapping("/professors/{professorId}/archive")
    public ActionResponse archive(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable UUID professorId) {
        return professorManagementService.archiveAccount(principal, professorId);
    }
}
