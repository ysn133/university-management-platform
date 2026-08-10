package com.platform.teachingassignment.rankpreference.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.teachingassignment.rankpreference.domain.TeachingAssignmentRankPreference;
import com.platform.teachingassignment.rankpreference.infrastructure.TeachingAssignmentRankPreferenceRepository;
import com.platform.teachingassignment.rankpreference.presentation.dto.ReplaceRankPreferencesRequest;
import com.platform.teachingassignment.rankpreference.presentation.dto.TeachingAssignmentRankPreferenceResponse;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import com.platform.usermanagement.professor.rank.domain.AcademicRank;
import com.platform.usermanagement.professor.rank.domain.AcademicRankStatus;
import com.platform.usermanagement.professor.rank.infrastructure.AcademicRankRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TeachingAssignmentRankPreferenceService {
    private final TeachingAssignmentRankPreferenceRepository preferenceRepository;
    private final AcademicRankRepository rankRepository;
    private final EstablishmentRepository establishmentRepository;
    private final AdminPermissionAuthorizationService authorizationService;

    public TeachingAssignmentRankPreferenceService(TeachingAssignmentRankPreferenceRepository preferenceRepository,
        AcademicRankRepository rankRepository, EstablishmentRepository establishmentRepository,
        AdminPermissionAuthorizationService authorizationService) {
        this.preferenceRepository = preferenceRepository;
        this.rankRepository = rankRepository;
        this.establishmentRepository = establishmentRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public List<TeachingAssignmentRankPreferenceResponse> list(AuthenticatedUserPrincipal principal, UUID establishmentId) {
        authorizationService.requirePermission(principal, establishmentId, PermissionCode.TEACHING_ASSIGNMENT_VIEW);
        ensureEstablishment(establishmentId);
        return preferenceRepository.findByEstablishmentIdOrderByComponentTypeAscPriorityAsc(establishmentId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<TeachingAssignmentRankPreferenceResponse> replace(AuthenticatedUserPrincipal principal,
        UUID establishmentId, TeachingComponentType componentType, ReplaceRankPreferencesRequest request) {
        authorizationService.requirePermission(principal, establishmentId, PermissionCode.TEACHING_ASSIGNMENT_CREATE);
        Establishment establishment = ensureEstablishment(establishmentId);
        Set<UUID> uniqueIds = new HashSet<>(request.academicRankIds());
        if (uniqueIds.size() != request.academicRankIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Academic ranks cannot be repeated");
        }
        List<AcademicRank> ranks = request.academicRankIds().stream().map(rankId -> {
            AcademicRank rank = rankRepository.findById(rankId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Academic rank not found"));
            if (!rank.getEstablishment().getId().equals(establishmentId) || rank.getStatus() != AcademicRankStatus.ACTIVE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Academic rank must be active and belong to the establishment");
            }
            if (componentType == TeachingComponentType.COURSE && !rank.canHoldModuleResponsibility()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course preferences require ranks eligible for module responsibility");
            }
            return rank;
        }).toList();

        preferenceRepository.deleteByEstablishmentIdAndComponentType(establishmentId, componentType);
        preferenceRepository.flush();
        for (int index = 0; index < ranks.size(); index++) {
            TeachingAssignmentRankPreference preference = new TeachingAssignmentRankPreference();
            preference.setEstablishment(establishment);
            preference.setComponentType(componentType);
            preference.setAcademicRank(ranks.get(index));
            preference.setPriority(index + 1);
            preference.setStatus(AcademicRankStatus.ACTIVE);
            preferenceRepository.save(preference);
        }
        return preferenceRepository.findByEstablishmentIdAndComponentTypeOrderByPriorityAsc(establishmentId, componentType).stream().map(this::toResponse).toList();
    }

    private Establishment ensureEstablishment(UUID id) { return establishmentRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Establishment not found")); }
    private TeachingAssignmentRankPreferenceResponse toResponse(TeachingAssignmentRankPreference preference) {
        AcademicRank rank = preference.getAcademicRank();
        return new TeachingAssignmentRankPreferenceResponse(preference.getId(), preference.getEstablishment().getId(), preference.getComponentType(), rank.getId(), rank.getCode(), rank.getName(), preference.getPriority());
    }
}
