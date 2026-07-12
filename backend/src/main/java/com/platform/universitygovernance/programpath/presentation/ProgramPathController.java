package com.platform.universitygovernance.programpath.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.programpath.application.ProgramPathService;
import com.platform.universitygovernance.programpath.presentation.dto.CreateProgramPathRequest;
import com.platform.universitygovernance.programpath.presentation.dto.ProgramPathResponse;
import com.platform.universitygovernance.programpath.presentation.dto.UpdateProgramPathRequest;
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
public class ProgramPathController {

    private final ProgramPathService programPathService;

    public ProgramPathController(ProgramPathService programPathService) {
        this.programPathService = programPathService;
    }

    @PostMapping("/establishments/{establishmentId}/program-paths")
    public ProgramPathResponse createProgramPath(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId,
        @Valid @RequestBody CreateProgramPathRequest request
    ) {
        return programPathService.createProgramPath(principal, establishmentId, request);
    }

    @GetMapping("/establishments/{establishmentId}/program-paths")
    public List<ProgramPathResponse> getProgramPaths(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId
    ) {
        return programPathService.getProgramPaths(principal, establishmentId);
    }

    @GetMapping("/program-paths/{programPathId}")
    public ProgramPathResponse getProgramPath(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID programPathId
    ) {
        return programPathService.getProgramPath(principal, programPathId);
    }

    @PatchMapping("/program-paths/{programPathId}")
    public ProgramPathResponse updateProgramPath(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID programPathId,
        @Valid @RequestBody UpdateProgramPathRequest request
    ) {
        return programPathService.updateProgramPath(principal, programPathId, request);
    }

    @DeleteMapping("/program-paths/{programPathId}")
    public ActionResponse deleteProgramPath(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID programPathId
    ) {
        return programPathService.deleteProgramPath(principal, programPathId);
    }
}
