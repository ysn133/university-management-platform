package com.platform.scheduling.examgroup.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.examgroup.application.ExamRoomAllocationService;
import com.platform.scheduling.examgroup.presentation.dto.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/module-exams/{moduleExamId}/room-allocations")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
public class ExamRoomAllocationController {
    private final ExamRoomAllocationService service;
    public ExamRoomAllocationController(ExamRoomAllocationService service) { this.service = service; }
    @GetMapping public List<ExamRoomAllocationResponse> get(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable UUID moduleExamId) { return service.get(principal, moduleExamId); }
    @PutMapping public List<ExamRoomAllocationResponse> replace(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable UUID moduleExamId, @Valid @RequestBody ReplaceExamRoomAllocationsRequest request) { return service.replace(principal, moduleExamId, request.allocations()); }
}
