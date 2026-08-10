package com.platform.usermanagement.professor.rank.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.usermanagement.professor.rank.application.AcademicRankService;
import com.platform.usermanagement.professor.rank.presentation.dto.AcademicRankRequest;
import com.platform.usermanagement.professor.rank.presentation.dto.AcademicRankResponse;
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
public class AcademicRankController {
    private final AcademicRankService service;
    public AcademicRankController(AcademicRankService service) { this.service = service; }

    @PostMapping("/establishments/{establishmentId}/academic-ranks")
    public AcademicRankResponse create(@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId, @Valid @RequestBody AcademicRankRequest request) {
        return service.create(principal, establishmentId, request);
    }

    @GetMapping("/establishments/{establishmentId}/academic-ranks")
    public List<AcademicRankResponse> list(@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId) { return service.list(principal, establishmentId); }

    @PutMapping("/academic-ranks/{rankId}")
    public AcademicRankResponse update(@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID rankId, @Valid @RequestBody AcademicRankRequest request) {
        return service.update(principal, rankId, request);
    }

    @DeleteMapping("/academic-ranks/{rankId}")
    public ActionResponse delete(@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID rankId) { return service.delete(principal, rankId); }
}
