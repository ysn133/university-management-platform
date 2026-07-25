package com.platform.scheduling.semesterschedule.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.semesterschedule.application.ScheduleEntryService;
import com.platform.scheduling.semesterschedule.presentation.dto.CreateScheduleEntryRequest;
import com.platform.scheduling.semesterschedule.presentation.dto.ScheduleEntryResponse;
import com.platform.scheduling.semesterschedule.presentation.dto.UpdateScheduleEntryRequest;
import com.platform.shared.presentation.ActionResponse;
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
public class ScheduleEntryController {

    private final ScheduleEntryService scheduleEntryService;

    public ScheduleEntryController(ScheduleEntryService scheduleEntryService) {
        this.scheduleEntryService = scheduleEntryService;
    }

    @PostMapping("/semester-schedules/{scheduleId}/entries")
    public ScheduleEntryResponse createScheduleEntry(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID scheduleId,
        @Valid @RequestBody CreateScheduleEntryRequest request
    ) {
        return scheduleEntryService.createScheduleEntry(principal, scheduleId, request);
    }

    @GetMapping("/semester-schedules/{scheduleId}/entries")
    public List<ScheduleEntryResponse> getScheduleEntries(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID scheduleId
    ) {
        return scheduleEntryService.getScheduleEntries(principal, scheduleId);
    }

    @GetMapping("/schedule-entries/{scheduleEntryId}")
    public ScheduleEntryResponse getScheduleEntry(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID scheduleEntryId
    ) {
        return scheduleEntryService.getScheduleEntry(principal, scheduleEntryId);
    }

    @PutMapping("/schedule-entries/{scheduleEntryId}")
    public ScheduleEntryResponse updateScheduleEntry(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID scheduleEntryId,
        @Valid @RequestBody UpdateScheduleEntryRequest request
    ) {
        return scheduleEntryService.updateScheduleEntry(
            principal,
            scheduleEntryId,
            request
        );
    }

    @DeleteMapping("/schedule-entries/{scheduleEntryId}")
    public ActionResponse deleteScheduleEntry(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID scheduleEntryId
    ) {
        return scheduleEntryService.deleteScheduleEntry(principal, scheduleEntryId);
    }
}
