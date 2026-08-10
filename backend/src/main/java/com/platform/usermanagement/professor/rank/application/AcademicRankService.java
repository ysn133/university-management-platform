package com.platform.usermanagement.professor.rank.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.infrastructure.ProfessorRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.teachingassignment.rankpreference.infrastructure.TeachingAssignmentRankPreferenceRepository;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.usermanagement.professor.rank.domain.AcademicRank;
import com.platform.usermanagement.professor.rank.infrastructure.AcademicRankRepository;
import com.platform.usermanagement.professor.rank.presentation.dto.AcademicRankRequest;
import com.platform.usermanagement.professor.rank.presentation.dto.AcademicRankResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AcademicRankService {
    private final AcademicRankRepository rankRepository;
    private final EstablishmentRepository establishmentRepository;
    private final ProfessorRepository professorRepository;
    private final TeachingAssignmentRankPreferenceRepository preferenceRepository;
    private final AdminPermissionAuthorizationService authorizationService;

    public AcademicRankService(AcademicRankRepository rankRepository, EstablishmentRepository establishmentRepository,
        ProfessorRepository professorRepository, TeachingAssignmentRankPreferenceRepository preferenceRepository,
        AdminPermissionAuthorizationService authorizationService) {
        this.rankRepository = rankRepository;
        this.establishmentRepository = establishmentRepository;
        this.professorRepository = professorRepository;
        this.preferenceRepository = preferenceRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public AcademicRankResponse create(AuthenticatedUserPrincipal principal, UUID establishmentId, AcademicRankRequest request) {
        authorizationService.requirePermission(principal, establishmentId, PermissionCode.PROFESSOR_UPDATE);
        Establishment establishment = establishmentRepository.findById(establishmentId).orElseThrow(() -> notFound("Establishment"));
        ensureUnique(establishmentId, request, null);
        AcademicRank rank = new AcademicRank();
        rank.setEstablishment(establishment);
        apply(rank, request);
        return toResponse(rankRepository.save(rank));
    }

    @Transactional(readOnly = true)
    public List<AcademicRankResponse> list(AuthenticatedUserPrincipal principal, UUID establishmentId) {
        authorizationService.requirePermission(principal, establishmentId, PermissionCode.PROFESSOR_VIEW);
        if (!establishmentRepository.existsById(establishmentId)) throw notFound("Establishment");
        return rankRepository.findByEstablishmentIdOrderBySeniorityOrderAsc(establishmentId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AcademicRankResponse update(AuthenticatedUserPrincipal principal, UUID rankId, AcademicRankRequest request) {
        AcademicRank rank = find(rankId);
        UUID establishmentId = rank.getEstablishment().getId();
        authorizationService.requirePermission(principal, establishmentId, PermissionCode.PROFESSOR_UPDATE);
        ensureUnique(establishmentId, request, rankId);
        apply(rank, request);
        return toResponse(rankRepository.save(rank));
    }

    @Transactional
    public ActionResponse delete(AuthenticatedUserPrincipal principal, UUID rankId) {
        AcademicRank rank = find(rankId);
        authorizationService.requirePermission(principal, rank.getEstablishment().getId(), PermissionCode.PROFESSOR_UPDATE);
        if (professorRepository.existsByAcademicRankId(rankId) || preferenceRepository.existsByAcademicRankId(rankId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Academic rank is currently in use");
        }
        rankRepository.delete(rank);
        return new ActionResponse(true, "Academic rank deleted");
    }

    private AcademicRank find(UUID id) { return rankRepository.findById(id).orElseThrow(() -> notFound("Academic rank")); }
    private ResponseStatusException notFound(String resource) { return new ResponseStatusException(HttpStatus.NOT_FOUND, resource + " not found"); }
    private void apply(AcademicRank rank, AcademicRankRequest request) {
        rank.setCode(request.code().trim().toUpperCase(Locale.ROOT));
        rank.setName(request.name().trim());
        rank.setSeniorityOrder(request.seniorityOrder());
        rank.setCanHoldModuleResponsibility(request.canHoldModuleResponsibility());
        rank.setStatus(request.status());
    }
    private void ensureUnique(UUID establishmentId, AcademicRankRequest request, UUID excludedId) {
        boolean codeExists = excludedId == null ? rankRepository.existsByEstablishmentIdAndCodeIgnoreCase(establishmentId, request.code().trim()) : rankRepository.existsByEstablishmentIdAndCodeIgnoreCaseAndIdNot(establishmentId, request.code().trim(), excludedId);
        boolean nameExists = excludedId == null ? rankRepository.existsByEstablishmentIdAndNameIgnoreCase(establishmentId, request.name().trim()) : rankRepository.existsByEstablishmentIdAndNameIgnoreCaseAndIdNot(establishmentId, request.name().trim(), excludedId);
        if (codeExists || nameExists) throw new ResponseStatusException(HttpStatus.CONFLICT, "Academic rank code and name must be unique in the establishment");
    }
    private AcademicRankResponse toResponse(AcademicRank rank) { return new AcademicRankResponse(rank.getId(), rank.getEstablishment().getId(), rank.getCode(), rank.getName(), rank.getSeniorityOrder(), rank.canHoldModuleResponsibility(), rank.getStatus()); }
}
