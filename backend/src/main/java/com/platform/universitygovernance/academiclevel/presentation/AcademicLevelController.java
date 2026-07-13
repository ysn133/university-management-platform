package com.platform.universitygovernance.academiclevel.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.academiclevel.application.AcademicLevelService;
import com.platform.universitygovernance.academiclevel.presentation.dto.AcademicLevelResponse;
import com.platform.universitygovernance.academiclevel.presentation.dto.CreateAcademicLevelRequest;
import com.platform.universitygovernance.academiclevel.presentation.dto.UpdateAcademicLevelRequest;
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
public class AcademicLevelController {

    private final AcademicLevelService academicLevelService;

    public AcademicLevelController(AcademicLevelService academicLevelService) {
        this.academicLevelService = academicLevelService;
    }

    @PostMapping("/program-filieres/{programFiliereId}/academic-levels")
    public AcademicLevelResponse createAcademicLevel(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID programFiliereId,
        @Valid @RequestBody CreateAcademicLevelRequest request
    ) {
        return academicLevelService.createAcademicLevel(principal, programFiliereId, request);
    }

    @GetMapping("/program-filieres/{programFiliereId}/academic-levels")
    public List<AcademicLevelResponse> getAcademicLevels(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID programFiliereId
    ) {
        return academicLevelService.getAcademicLevels(principal, programFiliereId);
    }

    @GetMapping("/academic-levels/{academicLevelId}")
    public AcademicLevelResponse getAcademicLevel(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicLevelId
    ) {
        return academicLevelService.getAcademicLevel(principal, academicLevelId);
    }

    @PutMapping("/academic-levels/{academicLevelId}")
    public AcademicLevelResponse updateAcademicLevel(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicLevelId,
        @Valid @RequestBody UpdateAcademicLevelRequest request
    ) {
        return academicLevelService.updateAcademicLevel(principal, academicLevelId, request);
    }

    @DeleteMapping("/academic-levels/{academicLevelId}")
    public ActionResponse deleteAcademicLevel(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicLevelId
    ) {
        return academicLevelService.deleteAcademicLevel(principal, academicLevelId);
    }
}
