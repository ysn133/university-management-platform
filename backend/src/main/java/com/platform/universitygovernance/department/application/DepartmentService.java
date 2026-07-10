package com.platform.universitygovernance.department.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.department.domain.Department;
import com.platform.universitygovernance.department.infrastructure.DepartmentRepository;
import com.platform.universitygovernance.department.presentation.dto.CreateDepartmentRequest;
import com.platform.universitygovernance.department.presentation.dto.DepartmentResponse;
import com.platform.universitygovernance.department.presentation.dto.UpdateDepartmentRequest;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EstablishmentRepository establishmentRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public DepartmentService(
        DepartmentRepository departmentRepository,
        EstablishmentRepository establishmentRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.departmentRepository = departmentRepository;
        this.establishmentRepository = establishmentRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public DepartmentResponse createDepartment(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        CreateDepartmentRequest request
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.DEPARTMENT_CREATE
        );

        Establishment establishment = findEstablishment(establishmentId);
        String name = normalizeName(request.name());
        ensureNameAvailable(establishmentId, name, null);

        Department department = new Department();
        department.setEstablishment(establishment);
        department.setName(name);

        return toResponse(departmentRepository.save(department));
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getDepartments(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.DEPARTMENT_VIEW
        );
        findEstablishment(establishmentId);

        return departmentRepository.findByEstablishmentIdOrderByNameAsc(establishmentId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartment(
        AuthenticatedUserPrincipal principal,
        UUID departmentId
    ) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));

        permissionAuthorizationService.requirePermission(
            principal,
            department.getEstablishment().getId(),
            PermissionCode.DEPARTMENT_VIEW
        );

        return toResponse(department);
    }

    @Transactional
    public DepartmentResponse updateDepartment(
        AuthenticatedUserPrincipal principal,
        UUID departmentId,
        UpdateDepartmentRequest request
    ) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        UUID establishmentId = department.getEstablishment().getId();

        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.DEPARTMENT_UPDATE
        );

        String name = normalizeName(request.name());
        ensureNameAvailable(establishmentId, name, departmentId);
        department.setName(name);

        return toResponse(departmentRepository.save(department));
    }

    @Transactional
    public ActionResponse deleteDepartment(
        AuthenticatedUserPrincipal principal,
        UUID departmentId
    ) {
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));

        permissionAuthorizationService.requirePermission(
            principal,
            department.getEstablishment().getId(),
            PermissionCode.DEPARTMENT_DELETE
        );

        departmentRepository.delete(department);
        return new ActionResponse(true, "Department deleted");
    }

    private Establishment findEstablishment(UUID establishmentId) {
        return establishmentRepository.findById(establishmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Establishment not found"
            ));
    }

    private void ensureNameAvailable(UUID establishmentId, String name, UUID departmentId) {
        boolean exists = departmentId == null
            ? departmentRepository.existsByEstablishmentIdAndNameIgnoreCase(establishmentId, name)
            : departmentRepository.existsByEstablishmentIdAndNameIgnoreCaseAndIdNot(
                establishmentId,
                name,
                departmentId
            );

        if (exists) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A department with this name already exists in the establishment"
            );
        }
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
            department.getId(),
            department.getEstablishment().getId(),
            department.getName(),
            department.getCreatedAt(),
            department.getUpdatedAt()
        );
    }
}
