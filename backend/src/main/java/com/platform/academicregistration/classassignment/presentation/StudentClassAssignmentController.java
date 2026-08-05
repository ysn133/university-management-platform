package com.platform.academicregistration.classassignment.presentation;

import com.platform.academicregistration.classassignment.application.StudentClassAssignmentService;
import com.platform.academicregistration.classassignment.presentation.dto.AssignStudentClassRequest;
import com.platform.academicregistration.classassignment.presentation.dto.BulkAssignStudentClassesRequest;
import com.platform.academicregistration.classassignment.presentation.dto.BulkClassAssignmentResponse;
import com.platform.academicregistration.classassignment.presentation.dto.ClassGroupRosterResponse;
import com.platform.academicregistration.classassignment.presentation.dto.StudentClassAssignmentResponse;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
public class StudentClassAssignmentController {

    private final StudentClassAssignmentService classAssignmentService;

    public StudentClassAssignmentController(
        StudentClassAssignmentService classAssignmentService
    ) {
        this.classAssignmentService = classAssignmentService;
    }

    @PutMapping(
        "/academic-registrations/{registrationId}/semesters/{semesterId}/class-assignment"
    )
    public StudentClassAssignmentResponse assignStudentClass(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID registrationId,
        @PathVariable UUID semesterId,
        @Valid @RequestBody AssignStudentClassRequest request
    ) {
        return classAssignmentService.assignStudentClass(
            principal,
            registrationId,
            semesterId,
            request
        );
    }

    @GetMapping(
        "/academic-registrations/{registrationId}/semesters/{semesterId}/class-assignment"
    )
    public StudentClassAssignmentResponse getStudentClassAssignment(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID registrationId,
        @PathVariable UUID semesterId
    ) {
        return classAssignmentService.getStudentClassAssignment(
            principal,
            registrationId,
            semesterId
        );
    }

    @PutMapping("/academic-levels/{academicLevelId}/class-groups/assignments")
    public BulkClassAssignmentResponse bulkAssignStudentClasses(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicLevelId,
        @RequestParam UUID academicYearId,
        @Valid @RequestBody BulkAssignStudentClassesRequest request
    ) {
        return classAssignmentService.bulkAssignStudentClasses(
            principal,
            academicLevelId,
            academicYearId,
            request
        );
    }

    @GetMapping("/academic-levels/{academicLevelId}/class-groups/roster")
    public ClassGroupRosterResponse getClassGroupRoster(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicLevelId,
        @RequestParam UUID academicYearId,
        @RequestParam UUID semesterId
    ) {
        return classAssignmentService.getClassGroupRoster(
            principal,
            academicLevelId,
            academicYearId,
            semesterId
        );
    }
}
