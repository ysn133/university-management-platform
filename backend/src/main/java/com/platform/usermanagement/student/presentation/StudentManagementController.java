package com.platform.usermanagement.student.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.usermanagement.student.application.StudentManagementService;
import com.platform.usermanagement.student.presentation.dto.CreateStudentRequest;
import com.platform.usermanagement.student.presentation.dto.CreateStudentResponse;
import com.platform.usermanagement.student.presentation.dto.StudentProfileResponse;
import com.platform.usermanagement.student.presentation.dto.UpdateStudentRequest;
import com.platform.usermanagement.shared.presentation.dto.ResetManagedPasswordRequest;
import com.platform.shared.presentation.ActionResponse;
import com.platform.identityaccess.domain.AccountStatus;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
public class StudentManagementController {

    private final StudentManagementService studentManagementService;

    public StudentManagementController(StudentManagementService studentManagementService) {
        this.studentManagementService = studentManagementService;
    }

    @PostMapping("/establishments/{establishmentId}/students")
    public CreateStudentResponse createStudent(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId,
        @Valid @RequestBody CreateStudentRequest request
    ) {
        return studentManagementService.createStudent(principal, establishmentId, request);
    }

    @GetMapping("/establishments/{establishmentId}/students")
    public List<StudentProfileResponse> getStudents(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId,
        @RequestParam(required = false) String query,
        @RequestParam(required = false) AccountStatus status,
        @RequestParam(required = false) LocalDate enrolledFrom,
        @RequestParam(required = false) LocalDate enrolledTo
    ) {
        return studentManagementService.getStudents(
            principal, establishmentId, query, status, enrolledFrom, enrolledTo
        );
    }

    @GetMapping("/students/{studentId}")
    public StudentProfileResponse getStudent(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID studentId
    ) {
        return studentManagementService.getStudent(principal, studentId);
    }

    @PutMapping("/students/{studentId}")
    public StudentProfileResponse updateStudent(@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID studentId, @Valid @RequestBody UpdateStudentRequest request) {
        return studentManagementService.updateStudent(principal, studentId, request);
    }

    @PostMapping("/students/{studentId}/password-reset")
    public ActionResponse resetPassword(@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID studentId, @Valid @RequestBody ResetManagedPasswordRequest request) {
        return studentManagementService.resetPassword(principal, studentId, request);
    }

    @PostMapping("/students/{studentId}/lock")
    public ActionResponse lock(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable UUID studentId) {
        return studentManagementService.lockAccount(principal, studentId);
    }

    @PostMapping("/students/{studentId}/unlock")
    public ActionResponse unlock(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable UUID studentId) {
        return studentManagementService.unlockAccount(principal, studentId);
    }

    @PostMapping("/students/{studentId}/deactivate")
    public ActionResponse deactivate(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable UUID studentId) {
        return studentManagementService.deactivateAccount(principal, studentId);
    }

    @PostMapping("/students/{studentId}/archive")
    public ActionResponse archive(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable UUID studentId) {
        return studentManagementService.archiveAccount(principal, studentId);
    }
}
