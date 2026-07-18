package com.platform.usermanagement.student.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.usermanagement.student.application.StudentManagementService;
import com.platform.usermanagement.student.presentation.dto.CreateStudentRequest;
import com.platform.usermanagement.student.presentation.dto.CreateStudentResponse;
import com.platform.usermanagement.student.presentation.dto.StudentProfileResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
        @PathVariable UUID establishmentId
    ) {
        return studentManagementService.getStudents(principal, establishmentId);
    }

    @GetMapping("/students/{studentId}")
    public StudentProfileResponse getStudent(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID studentId
    ) {
        return studentManagementService.getStudent(principal, studentId);
    }
}
