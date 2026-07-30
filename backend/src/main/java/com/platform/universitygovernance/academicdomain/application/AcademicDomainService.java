package com.platform.universitygovernance.academicdomain.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.academicdomain.domain.AcademicDomain;
import com.platform.universitygovernance.academicdomain.infrastructure.AcademicDomainRepository;
import com.platform.universitygovernance.academicdomain.presentation.dto.AcademicDomainResponse;
import com.platform.universitygovernance.academicdomain.presentation.dto.CreateAcademicDomainRequest;
import com.platform.universitygovernance.academicdomain.presentation.dto.UpdateAcademicDomainRequest;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AcademicDomainService {

    private final AcademicDomainRepository academicDomainRepository;
    private final EstablishmentRepository establishmentRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public AcademicDomainService(
        AcademicDomainRepository academicDomainRepository,
        EstablishmentRepository establishmentRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.academicDomainRepository = academicDomainRepository;
        this.establishmentRepository = establishmentRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public AcademicDomainResponse createAcademicDomain(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        CreateAcademicDomainRequest request
    ) {
        requirePermission(principal, establishmentId, PermissionCode.ACADEMIC_DOMAIN_CREATE);
        Establishment establishment = findEstablishment(establishmentId);
        String code = normalizeCode(request.code());
        ensureCodeAvailable(establishmentId, code, null);

        AcademicDomain academicDomain = new AcademicDomain();
        academicDomain.setEstablishment(establishment);
        academicDomain.setCode(code);
        academicDomain.setName(request.name().trim());
        return toResponse(academicDomainRepository.save(academicDomain));
    }

    @Transactional(readOnly = true)
    public List<AcademicDomainResponse> getAcademicDomains(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        requirePermission(principal, establishmentId, PermissionCode.ACADEMIC_DOMAIN_VIEW);
        findEstablishment(establishmentId);
        return academicDomainRepository.findByEstablishmentIdOrderByNameAsc(establishmentId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public AcademicDomainResponse getAcademicDomain(
        AuthenticatedUserPrincipal principal,
        UUID academicDomainId
    ) {
        AcademicDomain academicDomain = findAcademicDomain(academicDomainId);
        requirePermission(
            principal,
            academicDomain.getEstablishment().getId(),
            PermissionCode.ACADEMIC_DOMAIN_VIEW
        );
        return toResponse(academicDomain);
    }

    @Transactional
    public AcademicDomainResponse updateAcademicDomain(
        AuthenticatedUserPrincipal principal,
        UUID academicDomainId,
        UpdateAcademicDomainRequest request
    ) {
        AcademicDomain academicDomain = findAcademicDomain(academicDomainId);
        UUID establishmentId = academicDomain.getEstablishment().getId();
        requirePermission(principal, establishmentId, PermissionCode.ACADEMIC_DOMAIN_UPDATE);

        String code = normalizeCode(request.code());
        ensureCodeAvailable(establishmentId, code, academicDomainId);
        academicDomain.setCode(code);
        academicDomain.setName(request.name().trim());
        return toResponse(academicDomainRepository.save(academicDomain));
    }

    @Transactional
    public ActionResponse deleteAcademicDomain(
        AuthenticatedUserPrincipal principal,
        UUID academicDomainId
    ) {
        AcademicDomain academicDomain = findAcademicDomain(academicDomainId);
        requirePermission(
            principal,
            academicDomain.getEstablishment().getId(),
            PermissionCode.ACADEMIC_DOMAIN_DELETE
        );
        academicDomainRepository.delete(academicDomain);
        return new ActionResponse(true, "Academic domain deleted");
    }

    private Establishment findEstablishment(UUID establishmentId) {
        return establishmentRepository.findById(establishmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Establishment not found"
            ));
    }

    private AcademicDomain findAcademicDomain(UUID academicDomainId) {
        return academicDomainRepository.findById(academicDomainId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic domain not found"
            ));
    }

    private void ensureCodeAvailable(
        UUID establishmentId,
        String code,
        UUID academicDomainId
    ) {
        boolean exists = academicDomainId == null
            ? academicDomainRepository.existsByEstablishmentIdAndCodeIgnoreCase(
                establishmentId,
                code
            )
            : academicDomainRepository.existsByEstablishmentIdAndCodeIgnoreCaseAndIdNot(
                establishmentId,
                code,
                academicDomainId
            );
        if (exists) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "An academic domain with this code already exists in the establishment"
            );
        }
    }

    private void requirePermission(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        PermissionCode permissionCode
    ) {
        permissionAuthorizationService.requirePermission(principal, establishmentId, permissionCode);
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private AcademicDomainResponse toResponse(AcademicDomain academicDomain) {
        return new AcademicDomainResponse(
            academicDomain.getId(),
            academicDomain.getEstablishment().getId(),
            academicDomain.getCode(),
            academicDomain.getName(),
            academicDomain.getCreatedAt(),
            academicDomain.getUpdatedAt()
        );
    }
}
