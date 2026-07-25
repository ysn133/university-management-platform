package com.platform.scheduling.examschedule.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.examschedule.application.ExamScheduleService;
import com.platform.scheduling.examschedule.presentation.dto.CreateExamSchedule;
import com.platform.scheduling.examschedule.presentation.dto.ExamScheduleResponse;
import com.platform.scheduling.examschedule.presentation.dto.UpdateExamScheduleRequest;
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
public class ExamScheduleController {

    private final ExamScheduleService examScheduleService;

    public ExamScheduleController(ExamScheduleService examScheduleService) {
        this.examScheduleService = examScheduleService;
    }

    @PostMapping("/establishments/{establishmentId}/exam-schedules")
    public ExamScheduleResponse createExamSchedule(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId,
        @Valid @RequestBody CreateExamSchedule request
    ) {
        return examScheduleService.createExamSchedule(principal, establishmentId, request);
    }

    @GetMapping("/establishments/{establishmentId}/exam-schedules")
    public List<ExamScheduleResponse> getExamSchedules(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId
    ) {
        return examScheduleService.getExamSchedules(principal, establishmentId);
    }

    @GetMapping("/exam-schedules/{examScheduleId}")
    public ExamScheduleResponse getExamSchedule(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID examScheduleId
    ) {
        return examScheduleService.getExamSchedule(principal, examScheduleId);
    }

    @PutMapping("/exam-schedules/{examScheduleId}")
    public ExamScheduleResponse updateExamSchedule(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID examScheduleId,
        @Valid @RequestBody UpdateExamScheduleRequest request
    ) {
        return examScheduleService.updateExamSchedule(
            principal,
            examScheduleId,
            request
        );
    }

    @DeleteMapping("/exam-schedules/{examScheduleId}")
    public ActionResponse deleteExamSchedule(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID examScheduleId
    ) {
        return examScheduleService.deleteExamSchedule(principal, examScheduleId);
    }

    @PostMapping("/exam-schedules/{examScheduleId}/publish")
    public ExamScheduleResponse publishExamSchedule(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID examScheduleId
    ) {
        return examScheduleService.publishExamSchedule(principal, examScheduleId);
    }
}
