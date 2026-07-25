package com.platform.scheduling.moduleexam.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.moduleexam.application.ModuleExamService;
import com.platform.scheduling.moduleexam.presentation.dto.CreateModuleExamRequest;
import com.platform.scheduling.moduleexam.presentation.dto.ModuleExamResponse;
import com.platform.scheduling.moduleexam.presentation.dto.UpdateModuleExamRequest;
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
public class ModuleExamController {

    private final ModuleExamService moduleExamService;

    public ModuleExamController(ModuleExamService moduleExamService) {
        this.moduleExamService = moduleExamService;
    }

    @PostMapping("/exam-schedules/{examScheduleId}/module-exams")
    public ModuleExamResponse createModuleExam(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID examScheduleId,
        @Valid @RequestBody CreateModuleExamRequest request
    ) {
        return moduleExamService.createModuleExam(principal, examScheduleId, request);
    }

    @GetMapping("/exam-schedules/{examScheduleId}/module-exams")
    public List<ModuleExamResponse> getModuleExams(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID examScheduleId
    ) {
        return moduleExamService.getModuleExams(principal, examScheduleId);
    }

    @GetMapping("/module-exams/{moduleExamId}")
    public ModuleExamResponse getModuleExam(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID moduleExamId
    ) {
        return moduleExamService.getModuleExam(principal, moduleExamId);
    }

    @PutMapping("/module-exams/{moduleExamId}")
    public ModuleExamResponse updateModuleExam(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID moduleExamId,
        @Valid @RequestBody UpdateModuleExamRequest request
    ) {
        return moduleExamService.updateModuleExam(principal, moduleExamId, request);
    }

    @DeleteMapping("/module-exams/{moduleExamId}")
    public ActionResponse deleteModuleExam(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID moduleExamId
    ) {
        return moduleExamService.deleteModuleExam(principal, moduleExamId);
    }
}
