package com.platform.universitygovernance.degreecycle.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.degreecycle.application.DegreeCycleService;
import com.platform.universitygovernance.degreecycle.presentation.dto.CreateDegreeCycleRequest;
import com.platform.universitygovernance.degreecycle.presentation.dto.DegreeCycleResponse;
import com.platform.universitygovernance.degreecycle.presentation.dto.UpdateDegreeCycleRequest;
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
public class DegreeCycleController {

    private final DegreeCycleService degreeCycleService;

    public DegreeCycleController(DegreeCycleService degreeCycleService) {
        this.degreeCycleService = degreeCycleService;
    }

    @PostMapping("/establishments/{establishmentId}/degree-cycles")
    public DegreeCycleResponse createDegreeCycle(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId,
        @Valid @RequestBody CreateDegreeCycleRequest request
    ) {
        return degreeCycleService.createDegreeCycle(principal, establishmentId, request);
    }

    @GetMapping("/establishments/{establishmentId}/degree-cycles")
    public List<DegreeCycleResponse> getDegreeCycles(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId
    ) {
        return degreeCycleService.getDegreeCycles(principal, establishmentId);
    }

    @GetMapping("/degree-cycles/{degreeCycleId}")
    public DegreeCycleResponse getDegreeCycle(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID degreeCycleId
    ) {
        return degreeCycleService.getDegreeCycle(principal, degreeCycleId);
    }

    @PatchMapping("/degree-cycles/{degreeCycleId}")
    public DegreeCycleResponse updateDegreeCycle(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID degreeCycleId,
        @Valid @RequestBody UpdateDegreeCycleRequest request
    ) {
        return degreeCycleService.updateDegreeCycle(principal, degreeCycleId, request);
    }

    @DeleteMapping("/degree-cycles/{degreeCycleId}")
    public ActionResponse deleteDegreeCycle(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID degreeCycleId
    ) {
        return degreeCycleService.deleteDegreeCycle(principal, degreeCycleId);
    }
}
