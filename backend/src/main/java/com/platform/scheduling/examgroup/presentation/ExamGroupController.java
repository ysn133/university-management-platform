package com.platform.scheduling.examgroup.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.examgroup.application.ExamGroupService;
import com.platform.scheduling.examgroup.presentation.dto.ExamGroupPlanResponse;
import com.platform.scheduling.examgroup.presentation.dto.GenerateExamGroupsRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/exam-schedules/{examScheduleId}/class-groups/{classGroupId}/exam-groups")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
public class ExamGroupController {
    private final ExamGroupService service;
    public ExamGroupController(ExamGroupService service) { this.service = service; }
    @GetMapping public ExamGroupPlanResponse get(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable UUID examScheduleId, @PathVariable UUID classGroupId) { return service.getPlan(principal, examScheduleId, classGroupId); }
    @PostMapping("/generate") public ExamGroupPlanResponse generate(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable UUID examScheduleId, @PathVariable UUID classGroupId, @Valid @RequestBody GenerateExamGroupsRequest request) { return service.generate(principal, examScheduleId, classGroupId, request.splitCount()); }
}
