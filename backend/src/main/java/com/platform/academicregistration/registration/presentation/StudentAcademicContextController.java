package com.platform.academicregistration.registration.presentation;

import com.platform.academicregistration.registration.application.StudentAcademicContextService;
import com.platform.academicregistration.registration.presentation.dto.StudentAcademicContextResponse;
import com.platform.academicregistration.registration.presentation.dto.StudentModuleRegistrationResponse;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasRole('STUDENT')")
public class StudentAcademicContextController {

    private final StudentAcademicContextService service;

    public StudentAcademicContextController(StudentAcademicContextService service) {
        this.service = service;
    }

    @GetMapping("/me/academic-contexts")
    public List<StudentAcademicContextResponse> getMyAcademicContexts(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return service.getContexts(principal);
    }

    @GetMapping("/me/module-registrations")
    public List<StudentModuleRegistrationResponse> getMyModuleRegistrations(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return service.getModuleRegistrations(principal);
    }
}
