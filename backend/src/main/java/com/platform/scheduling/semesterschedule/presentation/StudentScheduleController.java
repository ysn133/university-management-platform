package com.platform.scheduling.semesterschedule.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.semesterschedule.application.ScheduleEntryService;
import com.platform.scheduling.semesterschedule.presentation.dto.StudentScheduleEntryResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasRole('STUDENT')")
public class StudentScheduleController {

    private final ScheduleEntryService scheduleEntryService;

    public StudentScheduleController(ScheduleEntryService scheduleEntryService) {
        this.scheduleEntryService = scheduleEntryService;
    }

    @GetMapping("/me/student-schedule-entries")
    public List<StudentScheduleEntryResponse> getMyScheduleEntries(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return scheduleEntryService.getStudentScheduleEntries(principal);
    }
}
