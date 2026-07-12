package com.platform.universitygovernance.programpath.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.universitygovernance.programpath.domain.ProgramPath;
import com.platform.universitygovernance.programpath.infrastructure.ProgramPathRepository;
import com.platform.universitygovernance.programpath.presentation.dto.CreateProgramPathRequest;
import com.platform.universitygovernance.programpath.presentation.dto.ProgramPathResponse;
import com.platform.universitygovernance.programpath.presentation.dto.UpdateProgramPathRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProgramPathService {

    private final ProgramPathRepository programPathRepository;
    private final EstablishmentRepository establishmentRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public ProgramPathService(
        ProgramPathRepository programPathRepository,
        EstablishmentRepository establishmentRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.programPathRepository = programPathRepository;
        this.establishmentRepository = establishmentRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public ProgramPathResponse createProgramPath(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        CreateProgramPathRequest request
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.PROGRAM_PATH_CREATE
        );

        Establishment establishment = findEstablishment(establishmentId);
        String name = normalizeName(request.name());
        ensureNameAvailable(establishmentId, name, null);

        ProgramPath programPath = new ProgramPath();
        programPath.setEstablishment(establishment);
        programPath.setName(name);

        return toResponse(programPathRepository.save(programPath));
    }

    @Transactional(readOnly = true)
    public List<ProgramPathResponse> getProgramPaths(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.PROGRAM_PATH_VIEW
        );
        findEstablishment(establishmentId);

        return programPathRepository.findByEstablishmentIdOrderByNameAsc(establishmentId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ProgramPathResponse getProgramPath(
        AuthenticatedUserPrincipal principal,
        UUID programPathId
    ) {
        ProgramPath programPath = findProgramPath(programPathId);
        permissionAuthorizationService.requirePermission(
            principal,
            programPath.getEstablishment().getId(),
            PermissionCode.PROGRAM_PATH_VIEW
        );
        return toResponse(programPath);
    }

    @Transactional
    public ProgramPathResponse updateProgramPath(
        AuthenticatedUserPrincipal principal,
        UUID programPathId,
        UpdateProgramPathRequest request
    ) {
        ProgramPath programPath = findProgramPath(programPathId);
        UUID establishmentId = programPath.getEstablishment().getId();
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.PROGRAM_PATH_UPDATE
        );

        String name = normalizeName(request.name());
        ensureNameAvailable(establishmentId, name, programPathId);
        programPath.setName(name);

        return toResponse(programPathRepository.save(programPath));
    }

    @Transactional
    public ActionResponse deleteProgramPath(
        AuthenticatedUserPrincipal principal,
        UUID programPathId
    ) {
        ProgramPath programPath = findProgramPath(programPathId);
        permissionAuthorizationService.requirePermission(
            principal,
            programPath.getEstablishment().getId(),
            PermissionCode.PROGRAM_PATH_DELETE
        );

        programPathRepository.delete(programPath);
        return new ActionResponse(true, "Program path deleted");
    }

    private Establishment findEstablishment(UUID establishmentId) {
        return establishmentRepository.findById(establishmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Establishment not found"
            ));
    }

    private ProgramPath findProgramPath(UUID programPathId) {
        return programPathRepository.findById(programPathId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Program path not found"));
    }

    private void ensureNameAvailable(UUID establishmentId, String name, UUID programPathId) {
        boolean exists = programPathId == null
            ? programPathRepository.existsByEstablishmentIdAndNameIgnoreCase(establishmentId, name)
            : programPathRepository.existsByEstablishmentIdAndNameIgnoreCaseAndIdNot(
                establishmentId,
                name,
                programPathId
            );

        if (exists) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A program path with this name already exists in the establishment"
            );
        }
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private ProgramPathResponse toResponse(ProgramPath programPath) {
        return new ProgramPathResponse(
            programPath.getId(),
            programPath.getEstablishment().getId(),
            programPath.getName(),
            programPath.getCreatedAt(),
            programPath.getUpdatedAt()
        );
    }
}
