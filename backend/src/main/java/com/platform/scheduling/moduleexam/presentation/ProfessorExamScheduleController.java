package com.platform.scheduling.moduleexam.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.moduleexam.application.ProfessorExamScheduleService;
import com.platform.scheduling.moduleexam.presentation.dto.ProfessorExamResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/exams")
@PreAuthorize("hasRole('PROFESSOR')")
public class ProfessorExamScheduleController {

    private final ProfessorExamScheduleService examScheduleService;

    public ProfessorExamScheduleController(ProfessorExamScheduleService examScheduleService) {
        this.examScheduleService = examScheduleService;
    }

    @GetMapping
    public List<ProfessorExamResponse> getMyExams(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return examScheduleService.getMyExams(principal);
    }
}
