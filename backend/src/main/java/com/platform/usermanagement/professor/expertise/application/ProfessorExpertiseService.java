package com.platform.usermanagement.professor.expertise.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.Professor;
import com.platform.identityaccess.infrastructure.ProfessorRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.academicdomain.domain.AcademicDomain;
import com.platform.universitygovernance.academicdomain.infrastructure.AcademicDomainRepository;
import com.platform.usermanagement.professor.expertise.domain.ProfessorExpertise;
import com.platform.usermanagement.professor.expertise.infrastructure.ProfessorExpertiseRepository;
import com.platform.usermanagement.professor.expertise.presentation.dto.ProfessorExpertiseItemResponse;
import com.platform.usermanagement.professor.expertise.presentation.dto.ProfessorExpertiseResponse;
import com.platform.usermanagement.professor.expertise.presentation.dto.ReplaceProfessorExpertiseRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfessorExpertiseService {

    private final ProfessorRepository professorRepository;
    private final AcademicDomainRepository academicDomainRepository;
    private final ProfessorExpertiseRepository professorExpertiseRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public ProfessorExpertiseService(
        ProfessorRepository professorRepository,
        AcademicDomainRepository academicDomainRepository,
        ProfessorExpertiseRepository professorExpertiseRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.professorRepository = professorRepository;
        this.academicDomainRepository = academicDomainRepository;
        this.professorExpertiseRepository = professorExpertiseRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional(readOnly = true)
    public ProfessorExpertiseResponse getProfessorExpertise(
        AuthenticatedUserPrincipal principal,
        UUID professorId
    ) {
        Professor professor = findProfessor(professorId);
        if (!isProfessorSelf(principal, professorId)) {
            permissionAuthorizationService.requirePermission(
                principal,
                professor.getEstablishment().getId(),
                PermissionCode.PROFESSOR_EXPERTISE_VIEW
            );
        }
        return toResponse(professorId);
    }

    @Transactional
    public ProfessorExpertiseResponse replaceProfessorExpertise(
        AuthenticatedUserPrincipal principal,
        UUID professorId,
        ReplaceProfessorExpertiseRequest request
    ) {
        Professor professor = findProfessor(professorId);
        UUID establishmentId = professor.getEstablishment().getId();
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.PROFESSOR_EXPERTISE_UPDATE
        );

        Set<UUID> domainIds = request.academicDomainIds();
        List<AcademicDomain> academicDomains = academicDomainRepository.findAllById(domainIds);
        if (academicDomains.size() != domainIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Academic domain not found");
        }
        if (academicDomains.stream().anyMatch(domain ->
            !establishmentId.equals(domain.getEstablishment().getId()))) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Academic domains must belong to the professor's establishment"
            );
        }

        List<ProfessorExpertise> currentExpertise =
            professorExpertiseRepository.findByProfessorIdOrderByAcademicDomainNameAsc(professorId);
        professorExpertiseRepository.deleteAll(currentExpertise);
        professorExpertiseRepository.flush();

        List<ProfessorExpertise> replacements = new ArrayList<>();
        for (AcademicDomain academicDomain : academicDomains) {
            ProfessorExpertise expertise = new ProfessorExpertise();
            expertise.setProfessor(professor);
            expertise.setAcademicDomain(academicDomain);
            replacements.add(expertise);
        }
        professorExpertiseRepository.saveAll(replacements);
        professorExpertiseRepository.flush();
        return toResponse(professorId);
    }

    private Professor findProfessor(UUID professorId) {
        return professorRepository.findById(professorId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Professor not found"
            ));
    }

    private boolean isProfessorSelf(
        AuthenticatedUserPrincipal principal,
        UUID professorId
    ) {
        return principal != null
            && principal.role() == AccountRoleType.PROFESSOR
            && professorId.equals(principal.roleEntityId());
    }

    private ProfessorExpertiseResponse toResponse(UUID professorId) {
        List<ProfessorExpertiseItemResponse> domains = professorExpertiseRepository
            .findByProfessorIdOrderByAcademicDomainNameAsc(professorId)
            .stream()
            .map(expertise -> new ProfessorExpertiseItemResponse(
                expertise.getAcademicDomain().getId(),
                expertise.getAcademicDomain().getCode(),
                expertise.getAcademicDomain().getName()
            ))
            .toList();
        return new ProfessorExpertiseResponse(professorId, domains);
    }
}
