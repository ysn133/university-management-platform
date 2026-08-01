package com.platform.attendance.absencerecord.presentation;

import com.platform.attendance.absencerecord.application.AbsenceRecordService;
import com.platform.attendance.absencerecord.presentation.dto.AbsenceRecordResponse;
import com.platform.attendance.absencerecord.presentation.dto.CreateAbsenceRequest;
import com.platform.attendance.absencerecord.presentation.dto.UpdateAbsenceJustificationRequest;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1")
public class AbsenceRecordController {

    private final AbsenceRecordService absenceRecordService;

    public AbsenceRecordController(AbsenceRecordService absenceRecordService) {
        this.absenceRecordService = absenceRecordService;
    }

    @PostMapping("/teaching-assignments/{teachingAssignmentId}/absences")
    @PreAuthorize("hasRole('PROFESSOR')")
    public AbsenceRecordResponse createAbsence(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID teachingAssignmentId,
        @Valid @RequestBody CreateAbsenceRequest request
    ) {
        return absenceRecordService.createAbsence(
            principal,
            teachingAssignmentId,
            request
        );
    }

    @GetMapping("/teaching-assignments/{teachingAssignmentId}/absences")
    @PreAuthorize("hasRole('PROFESSOR')")
    public List<AbsenceRecordResponse> getTeachingAssignmentAbsences(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID teachingAssignmentId
    ) {
        return absenceRecordService.getTeachingAssignmentAbsences(
            principal,
            teachingAssignmentId
        );
    }

    @GetMapping("/me/absences")
    @PreAuthorize("hasRole('STUDENT')")
    public List<AbsenceRecordResponse> getMyAbsences(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return absenceRecordService.getMyAbsences(principal);
    }

    @GetMapping("/establishments/{establishmentId}/absences")
    @PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    public List<AbsenceRecordResponse> getEstablishmentAbsences(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId,
        @RequestParam(required = false) UUID studentId,
        @RequestParam(required = false) UUID academicYearId,
        @RequestParam(required = false) UUID semesterId,
        @RequestParam(required = false) UUID subjectModuleId,
        @RequestParam(required = false) Boolean justified
    ) {
        return absenceRecordService.getEstablishmentAbsences(
            principal,
            establishmentId,
            studentId,
            academicYearId,
            semesterId,
            subjectModuleId,
            justified
        );
    }

    @PutMapping("/absences/{absenceId}/justification")
    @PreAuthorize("hasRole('PROFESSOR')")
    public AbsenceRecordResponse updateJustification(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID absenceId,
        @Valid @RequestBody UpdateAbsenceJustificationRequest request
    ) {
        return absenceRecordService.updateJustification(principal, absenceId, request);
    }

}
