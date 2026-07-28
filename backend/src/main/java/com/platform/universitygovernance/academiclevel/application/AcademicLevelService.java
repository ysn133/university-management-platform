package com.platform.universitygovernance.academiclevel.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academiclevel.infrastructure.AcademicLevelRepository;
import com.platform.universitygovernance.academiclevel.presentation.dto.AcademicLevelResponse;
import com.platform.universitygovernance.academiclevel.presentation.dto.CreateAcademicLevelRequest;
import com.platform.universitygovernance.academiclevel.presentation.dto.UpdateAcademicLevelRequest;
import com.platform.universitygovernance.academiclevelruleassignment.application.AcademicLevelRuleAssignmentService;
import com.platform.universitygovernance.programfiliere.domain.ProgramFiliere;
import com.platform.universitygovernance.programfiliere.infrastructure.ProgramFiliereRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AcademicLevelService {

    private final AcademicLevelRepository academicLevelRepository;
    private final ProgramFiliereRepository programFiliereRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;
    private final AcademicLevelRuleAssignmentService ruleAssignmentService;

    public AcademicLevelService(
        AcademicLevelRepository academicLevelRepository,
        ProgramFiliereRepository programFiliereRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService,
        AcademicLevelRuleAssignmentService ruleAssignmentService
    ) {
        this.academicLevelRepository = academicLevelRepository;
        this.programFiliereRepository = programFiliereRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
        this.ruleAssignmentService = ruleAssignmentService;
    }

    @Transactional
    public AcademicLevelResponse createAcademicLevel(
        AuthenticatedUserPrincipal principal,
        UUID programFiliereId,
        CreateAcademicLevelRequest request
    ) {
        ProgramFiliere programFiliere = findProgramFiliere(programFiliereId);
        requirePermission(principal, programFiliere, PermissionCode.ACADEMIC_LEVEL_CREATE);

        String name = normalizeName(request.name());
        ensureAcademicLevelAvailable(programFiliereId, name, request.levelOrder(), null);

        AcademicLevel academicLevel = new AcademicLevel();
        academicLevel.setProgramFiliere(programFiliere);
        academicLevel.setName(name);
        academicLevel.setLevelOrder(request.levelOrder());
        AcademicLevel savedAcademicLevel = academicLevelRepository.save(academicLevel);
        ruleAssignmentService.createInitialAssignment(
            principal,
            savedAcademicLevel,
            request.initialAcademicYearId(),
            request.academicRuleProfileId()
        );
        return toResponse(savedAcademicLevel);
    }

    @Transactional(readOnly = true)
    public List<AcademicLevelResponse> getAcademicLevels(
        AuthenticatedUserPrincipal principal,
        UUID programFiliereId
    ) {
        ProgramFiliere programFiliere = findProgramFiliere(programFiliereId);
        requirePermission(principal, programFiliere, PermissionCode.ACADEMIC_LEVEL_VIEW);
        return academicLevelRepository.findByProgramFiliereIdOrderByLevelOrderAsc(programFiliereId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public AcademicLevelResponse getAcademicLevel(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId
    ) {
        AcademicLevel academicLevel = findAcademicLevel(academicLevelId);
        requirePermission(principal, academicLevel.getProgramFiliere(), PermissionCode.ACADEMIC_LEVEL_VIEW);
        return toResponse(academicLevel);
    }

    @Transactional
    public AcademicLevelResponse updateAcademicLevel(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId,
        UpdateAcademicLevelRequest request
    ) {
        AcademicLevel academicLevel = findAcademicLevel(academicLevelId);
        ProgramFiliere programFiliere = academicLevel.getProgramFiliere();
        requirePermission(principal, programFiliere, PermissionCode.ACADEMIC_LEVEL_UPDATE);

        String name = normalizeName(request.name());
        ensureAcademicLevelAvailable(
            programFiliere.getId(),
            name,
            request.levelOrder(),
            academicLevelId
        );

        academicLevel.setName(name);
        academicLevel.setLevelOrder(request.levelOrder());
        return toResponse(academicLevelRepository.save(academicLevel));
    }

    @Transactional
    public ActionResponse deleteAcademicLevel(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId
    ) {
        AcademicLevel academicLevel = findAcademicLevel(academicLevelId);
        requirePermission(
            principal,
            academicLevel.getProgramFiliere(),
            PermissionCode.ACADEMIC_LEVEL_DELETE
        );
        academicLevelRepository.delete(academicLevel);
        return new ActionResponse(true, "Academic level deleted");
    }

    private ProgramFiliere findProgramFiliere(UUID programFiliereId) {
        return programFiliereRepository.findById(programFiliereId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Program/filiere not found"
            ));
    }

    private AcademicLevel findAcademicLevel(UUID academicLevelId) {
        return academicLevelRepository.findById(academicLevelId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic level not found"
            ));
    }

    private void requirePermission(
        AuthenticatedUserPrincipal principal,
        ProgramFiliere programFiliere,
        PermissionCode permissionCode
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            programFiliere.getDepartment().getEstablishment().getId(),
            permissionCode
        );
    }

    private void ensureAcademicLevelAvailable(
        UUID programFiliereId,
        String name,
        int levelOrder,
        UUID academicLevelId
    ) {
        boolean nameExists = academicLevelId == null
            ? academicLevelRepository.existsByProgramFiliereIdAndNameIgnoreCase(programFiliereId, name)
            : academicLevelRepository.existsByProgramFiliereIdAndNameIgnoreCaseAndIdNot(
                programFiliereId,
                name,
                academicLevelId
            );
        if (nameExists) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "An academic level with this name already exists in the program/filiere"
            );
        }

        boolean orderExists = academicLevelId == null
            ? academicLevelRepository.existsByProgramFiliereIdAndLevelOrder(programFiliereId, levelOrder)
            : academicLevelRepository.existsByProgramFiliereIdAndLevelOrderAndIdNot(
                programFiliereId,
                levelOrder,
                academicLevelId
            );
        if (orderExists) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "An academic level with this order already exists in the program/filiere"
            );
        }
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private AcademicLevelResponse toResponse(AcademicLevel academicLevel) {
        return new AcademicLevelResponse(
            academicLevel.getId(),
            academicLevel.getProgramFiliere().getId(),
            academicLevel.getProgramFiliere().getDepartment().getEstablishment().getId(),
            academicLevel.getName(),
            academicLevel.getLevelOrder(),
            academicLevel.getCreatedAt(),
            academicLevel.getUpdatedAt()
        );
    }
}
