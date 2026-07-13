package com.platform.universitygovernance.programfiliere.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.degreecycle.domain.DegreeCycle;
import com.platform.universitygovernance.degreecycle.infrastructure.DegreeCycleRepository;
import com.platform.universitygovernance.department.domain.Department;
import com.platform.universitygovernance.department.infrastructure.DepartmentRepository;
import com.platform.universitygovernance.programfiliere.domain.ProgramFiliere;
import com.platform.universitygovernance.programfiliere.infrastructure.ProgramFiliereRepository;
import com.platform.universitygovernance.programfiliere.presentation.dto.CreateProgramFiliereRequest;
import com.platform.universitygovernance.programfiliere.presentation.dto.ProgramFiliereResponse;
import com.platform.universitygovernance.programfiliere.presentation.dto.UpdateProgramFiliereRequest;
import com.platform.universitygovernance.programpath.domain.ProgramPath;
import com.platform.universitygovernance.programpath.infrastructure.ProgramPathRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProgramFiliereService {

    private final ProgramFiliereRepository programFiliereRepository;
    private final DepartmentRepository departmentRepository;
    private final DegreeCycleRepository degreeCycleRepository;
    private final ProgramPathRepository programPathRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public ProgramFiliereService(
        ProgramFiliereRepository programFiliereRepository,
        DepartmentRepository departmentRepository,
        DegreeCycleRepository degreeCycleRepository,
        ProgramPathRepository programPathRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.programFiliereRepository = programFiliereRepository;
        this.departmentRepository = departmentRepository;
        this.degreeCycleRepository = degreeCycleRepository;
        this.programPathRepository = programPathRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public ProgramFiliereResponse createProgramFiliere(
        AuthenticatedUserPrincipal principal,
        UUID departmentId,
        CreateProgramFiliereRequest request
    ) {
        Department department = findDepartment(departmentId);
        UUID establishmentId = department.getEstablishment().getId();
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.PROGRAM_FILIERE_CREATE
        );

        DegreeCycle degreeCycle = findDegreeCycle(request.degreeCycleId());
        ProgramPath programPath = findProgramPath(request.programPathId());
        ensureSameEstablishment(establishmentId, degreeCycle, programPath);

        String code = normalizeCode(request.code());
        ensureCodeAvailable(departmentId, degreeCycle.getId(), programPath.getId(), code, null);

        ProgramFiliere programFiliere = new ProgramFiliere();
        programFiliere.setDepartment(department);
        programFiliere.setDegreeCycle(degreeCycle);
        programFiliere.setProgramPath(programPath);
        programFiliere.setCode(code);
        programFiliere.setName(normalizeName(request.name()));
        return toResponse(programFiliereRepository.save(programFiliere));
    }

    @Transactional(readOnly = true)
    public List<ProgramFiliereResponse> getProgramFilieres(
        AuthenticatedUserPrincipal principal,
        UUID departmentId
    ) {
        Department department = findDepartment(departmentId);
        permissionAuthorizationService.requirePermission(
            principal,
            department.getEstablishment().getId(),
            PermissionCode.PROGRAM_FILIERE_VIEW
        );
        return programFiliereRepository.findByDepartmentIdOrderByNameAsc(departmentId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ProgramFiliereResponse getProgramFiliere(
        AuthenticatedUserPrincipal principal,
        UUID programFiliereId
    ) {
        ProgramFiliere programFiliere = findProgramFiliere(programFiliereId);
        permissionAuthorizationService.requirePermission(
            principal,
            programFiliere.getDepartment().getEstablishment().getId(),
            PermissionCode.PROGRAM_FILIERE_VIEW
        );
        return toResponse(programFiliere);
    }

    @Transactional
    public ProgramFiliereResponse updateProgramFiliere(
        AuthenticatedUserPrincipal principal,
        UUID programFiliereId,
        UpdateProgramFiliereRequest request
    ) {
        ProgramFiliere programFiliere = findProgramFiliere(programFiliereId);
        UUID establishmentId = programFiliere.getDepartment().getEstablishment().getId();
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.PROGRAM_FILIERE_UPDATE
        );

        DegreeCycle degreeCycle = findDegreeCycle(request.degreeCycleId());
        ProgramPath programPath = findProgramPath(request.programPathId());
        ensureSameEstablishment(establishmentId, degreeCycle, programPath);

        String code = normalizeCode(request.code());
        ensureCodeAvailable(
            programFiliere.getDepartment().getId(),
            degreeCycle.getId(),
            programPath.getId(),
            code,
            programFiliereId
        );

        programFiliere.setDegreeCycle(degreeCycle);
        programFiliere.setProgramPath(programPath);
        programFiliere.setCode(code);
        programFiliere.setName(normalizeName(request.name()));
        return toResponse(programFiliereRepository.save(programFiliere));
    }

    @Transactional
    public ActionResponse deleteProgramFiliere(
        AuthenticatedUserPrincipal principal,
        UUID programFiliereId
    ) {
        ProgramFiliere programFiliere = findProgramFiliere(programFiliereId);
        permissionAuthorizationService.requirePermission(
            principal,
            programFiliere.getDepartment().getEstablishment().getId(),
            PermissionCode.PROGRAM_FILIERE_DELETE
        );
        programFiliereRepository.delete(programFiliere);
        return new ActionResponse(true, "Program/filiere deleted");
    }

    private Department findDepartment(UUID departmentId) {
        return departmentRepository.findById(departmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
    }

    private DegreeCycle findDegreeCycle(UUID degreeCycleId) {
        return degreeCycleRepository.findById(degreeCycleId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Degree cycle not found"));
    }

    private ProgramPath findProgramPath(UUID programPathId) {
        return programPathRepository.findById(programPathId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Program path not found"));
    }

    private ProgramFiliere findProgramFiliere(UUID programFiliereId) {
        return programFiliereRepository.findById(programFiliereId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Program/filiere not found"));
    }

    private void ensureSameEstablishment(
        UUID establishmentId,
        DegreeCycle degreeCycle,
        ProgramPath programPath
    ) {
        if (!establishmentId.equals(degreeCycle.getEstablishment().getId())
            || !establishmentId.equals(programPath.getEstablishment().getId())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Department, degree cycle, and program path must belong to the same establishment"
            );
        }
    }

    private void ensureCodeAvailable(
        UUID departmentId,
        UUID degreeCycleId,
        UUID programPathId,
        String code,
        UUID programFiliereId
    ) {
        boolean exists = programFiliereId == null
            ? programFiliereRepository.existsByDepartmentIdAndDegreeCycleIdAndProgramPathIdAndCodeIgnoreCase(
                departmentId,
                degreeCycleId,
                programPathId,
                code
            )
            : programFiliereRepository
                .existsByDepartmentIdAndDegreeCycleIdAndProgramPathIdAndCodeIgnoreCaseAndIdNot(
                    departmentId,
                    degreeCycleId,
                    programPathId,
                    code,
                    programFiliereId
                );

        if (exists) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A program/filiere with this code already exists in the selected context"
            );
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private ProgramFiliereResponse toResponse(ProgramFiliere programFiliere) {
        return new ProgramFiliereResponse(
            programFiliere.getId(),
            programFiliere.getDepartment().getId(),
            programFiliere.getDepartment().getEstablishment().getId(),
            programFiliere.getDegreeCycle().getId(),
            programFiliere.getProgramPath().getId(),
            programFiliere.getCode(),
            programFiliere.getName(),
            programFiliere.getCreatedAt(),
            programFiliere.getUpdatedAt()
        );
    }
}
