package com.platform.universitygovernance.semester.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.semester.application.SemesterService;
import com.platform.universitygovernance.semester.presentation.dto.CreateSemesterRequest;
import com.platform.universitygovernance.semester.presentation.dto.SemesterResponse;
import com.platform.universitygovernance.semester.presentation.dto.UpdateSemesterRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
public class SemesterController {

    private final SemesterService semesterService;

    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    @PostMapping("/academic-levels/{academicLevelId}/semesters")
    public SemesterResponse createSemester(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicLevelId,
        @RequestParam UUID academicYearId,
        @Valid @RequestBody CreateSemesterRequest request
    ) {
        return semesterService.createSemester(principal, academicLevelId, academicYearId, request);
    }

    @GetMapping("/academic-levels/{academicLevelId}/semesters")
    public List<SemesterResponse> getSemesters(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicLevelId,
        @RequestParam UUID academicYearId
    ) {
        return semesterService.getSemesters(principal, academicLevelId, academicYearId);
    }

    @GetMapping("/semesters/{semesterId}")
    public SemesterResponse getSemester(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID semesterId
    ) {
        return semesterService.getSemester(principal, semesterId);
    }

    @PutMapping("/semesters/{semesterId}")
    public SemesterResponse updateSemester(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID semesterId,
        @Valid @RequestBody UpdateSemesterRequest request
    ) {
        return semesterService.updateSemester(principal, semesterId, request);
    }

    @DeleteMapping("/semesters/{semesterId}")
    public ActionResponse deleteSemester(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID semesterId
    ) {
        return semesterService.deleteSemester(principal, semesterId);
    }
}
