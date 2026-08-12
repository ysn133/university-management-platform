package com.platform.scheduling.semesterschedule.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.semesterschedule.application.ScheduleEntryService;
import com.platform.scheduling.semesterschedule.presentation.dto.ScheduleEntryResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasRole('PROFESSOR')")
public class ProfessorScheduleController {

    private final ScheduleEntryService scheduleEntryService;

    public ProfessorScheduleController(ScheduleEntryService scheduleEntryService) {
        this.scheduleEntryService = scheduleEntryService;
    }

    @GetMapping("/me/schedule-entries")
    public List<ScheduleEntryResponse> getMyScheduleEntries(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return scheduleEntryService.getMyScheduleEntries(principal);
    }
}
