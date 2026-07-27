package com.platform.assessment.graderecord.presentation;

import com.platform.assessment.graderecord.application.GradeRecordService;
import com.platform.assessment.graderecord.presentation.dto.GradeSheetResponse;
import com.platform.assessment.graderecord.presentation.dto.SaveGradeSheetRequest;
import com.platform.assessment.graderecord.presentation.dto.StudentGradeResponse;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
public class GradeRecordController {

    private final GradeRecordService gradeRecordService;

    public GradeRecordController(GradeRecordService gradeRecordService) {
        this.gradeRecordService = gradeRecordService;
    }

    @GetMapping("/module-exams/{moduleExamId}/grade-sheet")
    @PreAuthorize(
        "hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN', 'PROFESSOR')"
    )
    public GradeSheetResponse getGradeSheet(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID moduleExamId
    ) {
        return gradeRecordService.getGradeSheet(principal, moduleExamId);
    }

    @PutMapping("/module-exams/{moduleExamId}/grade-sheet")
    @PreAuthorize("hasRole('PROFESSOR')")
    public GradeSheetResponse saveDraftGradeSheet(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID moduleExamId,
        @Valid @RequestBody SaveGradeSheetRequest request
    ) {
        return gradeRecordService.saveDraftGradeSheet(
            principal,
            moduleExamId,
            request
        );
    }

    @PostMapping("/module-exams/{moduleExamId}/grade-sheet/submit")
    @PreAuthorize("hasRole('PROFESSOR')")
    public GradeSheetResponse submitGradeSheet(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID moduleExamId
    ) {
        return gradeRecordService.submitGradeSheet(principal, moduleExamId);
    }

    @PostMapping("/module-exams/{moduleExamId}/grade-sheet/review")
    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    public GradeSheetResponse reviewGradeSheet(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID moduleExamId
    ) {
        return gradeRecordService.reviewGradeSheet(principal, moduleExamId);
    }

    @PostMapping("/module-exams/{moduleExamId}/grade-sheet/approve")
    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    public GradeSheetResponse approveGradeSheet(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID moduleExamId
    ) {
        return gradeRecordService.approveGradeSheet(principal, moduleExamId);
    }

    @PostMapping("/module-exams/{moduleExamId}/grade-sheet/publish")
    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    public GradeSheetResponse publishGradeSheet(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID moduleExamId
    ) {
        return gradeRecordService.publishGradeSheet(principal, moduleExamId);
    }

    @GetMapping("/me/grades")
    @PreAuthorize("hasRole('STUDENT')")
    public List<StudentGradeResponse> getMyGrades(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @RequestParam(required = false) UUID academicYearId,
        @RequestParam(required = false) UUID academicLevelId,
        @RequestParam(required = false) UUID semesterId
    ) {
        return gradeRecordService.getMyGrades(
            principal,
            academicYearId,
            academicLevelId,
            semesterId
        );
    }

    @GetMapping("/students/{studentId}/grades")
    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    public List<StudentGradeResponse> getStudentGrades(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID studentId,
        @RequestParam(required = false) UUID academicYearId,
        @RequestParam(required = false) UUID academicLevelId,
        @RequestParam(required = false) UUID semesterId
    ) {
        return gradeRecordService.getStudentGrades(
            principal,
            studentId,
            academicYearId,
            academicLevelId,
            semesterId
        );
    }
}
