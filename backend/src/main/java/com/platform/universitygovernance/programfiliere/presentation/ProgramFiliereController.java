package com.platform.universitygovernance.programfiliere.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.programfiliere.application.ProgramFiliereService;
import com.platform.universitygovernance.programfiliere.presentation.dto.CreateProgramFiliereRequest;
import com.platform.universitygovernance.programfiliere.presentation.dto.ProgramFiliereResponse;
import com.platform.universitygovernance.programfiliere.presentation.dto.UpdateProgramFiliereRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
public class ProgramFiliereController {

    private final ProgramFiliereService programFiliereService;

    public ProgramFiliereController(ProgramFiliereService programFiliereService) {
        this.programFiliereService = programFiliereService;
    }

    @PostMapping("/departments/{departmentId}/program-filieres")
    public ProgramFiliereResponse createProgramFiliere(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID departmentId,
        @Valid @RequestBody CreateProgramFiliereRequest request
    ) {
        return programFiliereService.createProgramFiliere(principal, departmentId, request);
    }

    @GetMapping("/departments/{departmentId}/program-filieres")
    public List<ProgramFiliereResponse> getProgramFilieres(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID departmentId
    ) {
        return programFiliereService.getProgramFilieres(principal, departmentId);
    }

    @GetMapping("/program-filieres/{programFiliereId}")
    public ProgramFiliereResponse getProgramFiliere(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID programFiliereId
    ) {
        return programFiliereService.getProgramFiliere(principal, programFiliereId);
    }

    @PatchMapping("/program-filieres/{programFiliereId}")
    public ProgramFiliereResponse updateProgramFiliere(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID programFiliereId,
        @Valid @RequestBody UpdateProgramFiliereRequest request
    ) {
        return programFiliereService.updateProgramFiliere(principal, programFiliereId, request);
    }

    @DeleteMapping("/program-filieres/{programFiliereId}")
    public ActionResponse deleteProgramFiliere(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID programFiliereId
    ) {
        return programFiliereService.deleteProgramFiliere(principal, programFiliereId);
    }
}
