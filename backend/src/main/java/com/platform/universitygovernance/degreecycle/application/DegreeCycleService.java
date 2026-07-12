package com.platform.universitygovernance.degreecycle.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.degreecycle.domain.DegreeCycle;
import com.platform.universitygovernance.degreecycle.infrastructure.DegreeCycleRepository;
import com.platform.universitygovernance.degreecycle.presentation.dto.CreateDegreeCycleRequest;
import com.platform.universitygovernance.degreecycle.presentation.dto.DegreeCycleResponse;
import com.platform.universitygovernance.degreecycle.presentation.dto.UpdateDegreeCycleRequest;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DegreeCycleService {

    private final DegreeCycleRepository degreeCycleRepository;
    private final EstablishmentRepository establishmentRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public DegreeCycleService(
        DegreeCycleRepository degreeCycleRepository,
        EstablishmentRepository establishmentRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.degreeCycleRepository = degreeCycleRepository;
        this.establishmentRepository = establishmentRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public DegreeCycleResponse createDegreeCycle(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        CreateDegreeCycleRequest request
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.DEGREE_CYCLE_CREATE
        );

        Establishment establishment = findEstablishment(establishmentId);
        String name = normalizeName(request.name());
        ensureNameAvailable(establishmentId, name, null);

        DegreeCycle degreeCycle = new DegreeCycle();
        degreeCycle.setEstablishment(establishment);
        degreeCycle.setName(name);
        return toResponse(degreeCycleRepository.save(degreeCycle));
    }

    @Transactional(readOnly = true)
    public List<DegreeCycleResponse> getDegreeCycles(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.DEGREE_CYCLE_VIEW
        );
        findEstablishment(establishmentId);

        return degreeCycleRepository.findByEstablishmentIdOrderByNameAsc(establishmentId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public DegreeCycleResponse getDegreeCycle(
        AuthenticatedUserPrincipal principal,
        UUID degreeCycleId
    ) {
        DegreeCycle degreeCycle = findDegreeCycle(degreeCycleId);
        permissionAuthorizationService.requirePermission(
            principal,
            degreeCycle.getEstablishment().getId(),
            PermissionCode.DEGREE_CYCLE_VIEW
        );
        return toResponse(degreeCycle);
    }

    @Transactional
    public DegreeCycleResponse updateDegreeCycle(
        AuthenticatedUserPrincipal principal,
        UUID degreeCycleId,
        UpdateDegreeCycleRequest request
    ) {
        DegreeCycle degreeCycle = findDegreeCycle(degreeCycleId);
        UUID establishmentId = degreeCycle.getEstablishment().getId();
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.DEGREE_CYCLE_UPDATE
        );

        String name = normalizeName(request.name());
        ensureNameAvailable(establishmentId, name, degreeCycleId);
        degreeCycle.setName(name);
        return toResponse(degreeCycleRepository.save(degreeCycle));
    }

    @Transactional
    public ActionResponse deleteDegreeCycle(
        AuthenticatedUserPrincipal principal,
        UUID degreeCycleId
    ) {
        DegreeCycle degreeCycle = findDegreeCycle(degreeCycleId);
        permissionAuthorizationService.requirePermission(
            principal,
            degreeCycle.getEstablishment().getId(),
            PermissionCode.DEGREE_CYCLE_DELETE
        );
        degreeCycleRepository.delete(degreeCycle);
        return new ActionResponse(true, "Degree cycle deleted");
    }

    private Establishment findEstablishment(UUID establishmentId) {
        return establishmentRepository.findById(establishmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Establishment not found"
            ));
    }

    private DegreeCycle findDegreeCycle(UUID degreeCycleId) {
        return degreeCycleRepository.findById(degreeCycleId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Degree cycle not found"));
    }

    private void ensureNameAvailable(UUID establishmentId, String name, UUID degreeCycleId) {
        boolean exists = degreeCycleId == null
            ? degreeCycleRepository.existsByEstablishmentIdAndNameIgnoreCase(establishmentId, name)
            : degreeCycleRepository.existsByEstablishmentIdAndNameIgnoreCaseAndIdNot(
                establishmentId,
                name,
                degreeCycleId
            );

        if (exists) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A degree cycle with this name already exists in the establishment"
            );
        }
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private DegreeCycleResponse toResponse(DegreeCycle degreeCycle) {
        return new DegreeCycleResponse(
            degreeCycle.getId(),
            degreeCycle.getEstablishment().getId(),
            degreeCycle.getName(),
            degreeCycle.getCreatedAt(),
            degreeCycle.getUpdatedAt()
        );
    }
}
