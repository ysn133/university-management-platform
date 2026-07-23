package com.platform.usermanagement.professor.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.usermanagement.professor.application.ProfessorManagementService;
import com.platform.usermanagement.professor.presentation.dto.CreateProfessorRequest;
import com.platform.usermanagement.professor.presentation.dto.CreateProfessorResponse;
import com.platform.usermanagement.professor.presentation.dto.ProfessorProfileResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
        @PathVariable UUID establishmentId
    ) {
        return professorManagementService.getProfessors(principal, establishmentId);
    }

    @GetMapping("/professors/{professorId}")
    public ProfessorProfileResponse getProfessor(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID professorId
    ) {
        return professorManagementService.getProfessor(principal, professorId);
    }
}
