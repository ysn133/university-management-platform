package com.platform.universitygovernance.establishment.application;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.domain.EstablishmentStatus;
import com.platform.universitygovernance.establishment.domain.EstablishmentType;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.universitygovernance.establishment.presentation.dto.CreateEstablishmentRequest;
import com.platform.universitygovernance.establishment.presentation.dto.EstablishmentResponse;
import com.platform.universitygovernance.establishment.presentation.dto.UpdateEstablishmentRequest;
import com.platform.universitygovernance.university.domain.University;
import com.platform.universitygovernance.university.infrastructure.UniversityRepository;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EstablishmentService {

    private final EstablishmentRepository establishmentRepository;
    private final UniversityRepository universityRepository;
    
    public EstablishmentService(
        EstablishmentRepository establishmentRepository,
        UniversityRepository universityRepository
    ) {
        this.establishmentRepository = establishmentRepository;
        this.universityRepository = universityRepository;
    }

    @Transactional(readOnly = true)
    public EstablishmentResponse getEstablishment(UUID id) {
        return toResponse(findEstablishment(id));
    }

    @Transactional(readOnly = true)
    public EstablishmentResponse getEstablishment(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        boolean establishmentMember = principal.role() == AccountRoleType.SUPER_ADMIN
            || principal.role() == AccountRoleType.ADMIN;
        if (principal.role() != AccountRoleType.ROOT_SUPER_ADMIN
            && (!establishmentMember
                || !establishmentId.equals(principal.establishmentId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for this establishment");
        }
        return toResponse(findEstablishment(establishmentId));
    }

    @Transactional(readOnly = true)
    public List<EstablishmentResponse> getEstablishments(
        UUID universityId,
        String query,
        EstablishmentType type,
        EstablishmentStatus status
    ) {
        if (!universityRepository.existsById(universityId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "University not found");
        }

        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();

        return establishmentRepository.searchByUniversity(universityId, normalizedQuery, type, status)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public EstablishmentResponse createEstablishment(CreateEstablishmentRequest request) {
        University university = universityRepository
            .findById(request.universityId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "University not found"
            ));

        Establishment establishment = new Establishment();
        establishment.setName(request.name().trim());
        establishment.setUniversity(university);
        establishment.setEstablishmentType(request.type());
        establishment.setEstablishmentStatus(EstablishmentStatus.ACTIVE);

        return toResponse(establishmentRepository.save(establishment));
    }

    @Transactional
    public EstablishmentResponse updateEstablishment(
        UUID establishmentId,
        UpdateEstablishmentRequest request
    ) {
        Establishment establishment = findEstablishment(establishmentId);
        establishment.setName(request.name().trim());
        establishment.setEstablishmentType(request.type());
        return toResponse(establishmentRepository.save(establishment));
    }

    @Transactional
    public void activateEstablishment(UUID establishmentId) {
        Establishment establishment = findEstablishment(establishmentId);
        establishment.setEstablishmentStatus(EstablishmentStatus.ACTIVE);
        establishmentRepository.save(establishment);
    }

    @Transactional
    public void deactivateEstablishment(UUID establishmentId) {
        Establishment establishment = findEstablishment(establishmentId);
        establishment.setEstablishmentStatus(EstablishmentStatus.INACTIVE);
        establishmentRepository.save(establishment);
    }

    private Establishment findEstablishment(UUID establishmentId) {
        return establishmentRepository.findById(establishmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Establishment not found"));
    }

    private EstablishmentResponse toResponse(Establishment establishment) {
        return new EstablishmentResponse(
            establishment.getId(),
            establishment.getUniversity().getId(),
            establishment.getName(),
            establishment.getEstablishmentType(),
            establishment.getEstablishmentStatus(),
            establishment.getCreatedAt(),
            establishment.getUpdatedAt()
        );
    }
}
