package com.platform.universitygovernance.department.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.department.application.DepartmentService;
import com.platform.universitygovernance.department.presentation.dto.CreateDepartmentRequest;
import com.platform.universitygovernance.department.presentation.dto.DepartmentResponse;
import com.platform.universitygovernance.department.presentation.dto.UpdateDepartmentRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping("/establishments/{establishmentId}/departments")
    public DepartmentResponse createDepartment(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId,
        @Valid @RequestBody CreateDepartmentRequest request
    ) {
        return departmentService.createDepartment(principal, establishmentId, request);
    }

    @GetMapping("/establishments/{establishmentId}/departments")
    public List<DepartmentResponse> getDepartments(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId
    ) {
        return departmentService.getDepartments(principal, establishmentId);
    }

    @GetMapping("/departments/{departmentId}")
    public DepartmentResponse getDepartment(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID departmentId
    ) {
        return departmentService.getDepartment(principal, departmentId);
    }

    @PatchMapping("/departments/{departmentId}")
    public DepartmentResponse updateDepartment(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID departmentId,
        @Valid @RequestBody UpdateDepartmentRequest request
    ) {
        return departmentService.updateDepartment(principal, departmentId, request);
    }

    @DeleteMapping("/departments/{departmentId}")
    public ActionResponse deleteDepartment(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID departmentId
    ) {
        return departmentService.deleteDepartment(principal, departmentId);
    }
}
