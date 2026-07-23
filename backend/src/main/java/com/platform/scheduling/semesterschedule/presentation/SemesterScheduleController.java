package com.platform.scheduling.semesterschedule.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.semesterschedule.application.SemesterScheduleService;
import com.platform.scheduling.semesterschedule.presentation.dto.CreateSemesterScheduleRequest;
import com.platform.scheduling.semesterschedule.presentation.dto.SemesterScheduleResponse;
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
public class SemesterScheduleController {

    private final SemesterScheduleService semesterScheduleService;

    public SemesterScheduleController(SemesterScheduleService semesterScheduleService) {
        this.semesterScheduleService = semesterScheduleService;
    }

    @PostMapping("/establishments/{establishmentId}/semester-schedules")
    public SemesterScheduleResponse createSemesterSchedule(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId,
        @Valid @RequestBody CreateSemesterScheduleRequest request
    ) {
        return semesterScheduleService.createSemesterSchedule(
            principal,
            establishmentId,
            request
        );
    }

    @GetMapping("/establishments/{establishmentId}/semester-schedules")
    public List<SemesterScheduleResponse> getSemesterSchedules(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId
    ) {
        return semesterScheduleService.getSemesterSchedules(principal, establishmentId);
    }

    @GetMapping("/semester-schedules/{scheduleId}")
    public SemesterScheduleResponse getSemesterSchedule(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID scheduleId
    ) {
        return semesterScheduleService.getSemesterSchedule(principal, scheduleId);
    }

    @PostMapping("/semester-schedules/{scheduleId}/publish")
    public SemesterScheduleResponse publishSemesterSchedule(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID scheduleId
    ) {
        return semesterScheduleService.publishSemesterSchedule(principal, scheduleId);
    }
}
