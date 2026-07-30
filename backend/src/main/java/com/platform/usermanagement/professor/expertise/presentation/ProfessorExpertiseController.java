package com.platform.usermanagement.professor.expertise.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.usermanagement.professor.expertise.application.ProfessorExpertiseService;
import com.platform.usermanagement.professor.expertise.presentation.dto.ProfessorExpertiseResponse;
import com.platform.usermanagement.professor.expertise.presentation.dto.ReplaceProfessorExpertiseRequest;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/professors/{professorId}/expertise")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'PROFESSOR')")
public class ProfessorExpertiseController {

    private final ProfessorExpertiseService professorExpertiseService;

    public ProfessorExpertiseController(ProfessorExpertiseService professorExpertiseService) {
        this.professorExpertiseService = professorExpertiseService;
    }

    @GetMapping
    public ProfessorExpertiseResponse getProfessorExpertise(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID professorId
    ) {
        return professorExpertiseService.getProfessorExpertise(principal, professorId);
    }

    @PutMapping
    public ProfessorExpertiseResponse replaceProfessorExpertise(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID professorId,
        @Valid @RequestBody ReplaceProfessorExpertiseRequest request
    ) {
        return professorExpertiseService.replaceProfessorExpertise(principal, professorId, request);
    }
}
