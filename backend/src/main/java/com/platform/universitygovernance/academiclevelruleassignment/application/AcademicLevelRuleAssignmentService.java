package com.platform.universitygovernance.academiclevelruleassignment.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academiclevel.infrastructure.AcademicLevelRepository;
import com.platform.universitygovernance.academiclevelruleassignment.domain.AcademicLevelRuleAssignment;
import com.platform.universitygovernance.academiclevelruleassignment.domain.AcademicLevelRuleAssignmentStatus;
import com.platform.universitygovernance.academiclevelruleassignment.infrastructure.AcademicLevelRuleAssignmentRepository;
import com.platform.universitygovernance.academiclevelruleassignment.presentation.dto.AcademicLevelRuleAssignmentResponse;
import com.platform.universitygovernance.academiclevelruleassignment.presentation.dto.CreateAcademicLevelRuleAssignmentRequest;
import com.platform.universitygovernance.academiclevelruleassignment.presentation.dto.UpdateAcademicLevelRuleAssignmentRequest;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfile;
import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfileStatus;
import com.platform.universitygovernance.academicruleprofile.infrastructure.AcademicRuleProfileRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AcademicLevelRuleAssignmentService {

    private final AcademicLevelRuleAssignmentRepository assignmentRepository;
    private final AcademicLevelRepository academicLevelRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AcademicRuleProfileRepository academicRuleProfileRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public AcademicLevelRuleAssignmentService(
        AcademicLevelRuleAssignmentRepository assignmentRepository,
        AcademicLevelRepository academicLevelRepository,
        AcademicYearRepository academicYearRepository,
        AcademicRuleProfileRepository academicRuleProfileRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.assignmentRepository = assignmentRepository;
        this.academicLevelRepository = academicLevelRepository;
        this.academicYearRepository = academicYearRepository;
        this.academicRuleProfileRepository = academicRuleProfileRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public AcademicLevelRuleAssignmentResponse createAssignment(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId,
        CreateAcademicLevelRuleAssignmentRequest request
    ) {
        AcademicLevel academicLevel = findAcademicLevel(academicLevelId);
        return createAssignment(
            principal,
            academicLevel,
            request.academicYearId(),
            request.academicRuleProfileId()
        );
    }

    @Transactional
    public AcademicLevelRuleAssignmentResponse createInitialAssignment(
        AuthenticatedUserPrincipal principal,
        AcademicLevel academicLevel,
        UUID academicYearId,
        UUID academicRuleProfileId
    ) {
        return createAssignment(
            principal,
            academicLevel,
            academicYearId,
            academicRuleProfileId
        );
    }

    @Transactional(readOnly = true)
    public List<AcademicLevelRuleAssignmentResponse> getAssignments(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId
    ) {
        AcademicLevel academicLevel = findAcademicLevel(academicLevelId);
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId(academicLevel),
            PermissionCode.ACADEMIC_RULE_ASSIGNMENT_VIEW
        );
        return assignmentRepository
            .findByAcademicLevelIdOrderByAcademicYearStartYearDesc(academicLevelId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public AcademicLevelRuleAssignmentResponse getAssignment(
        AuthenticatedUserPrincipal principal,
        UUID assignmentId
    ) {
        AcademicLevelRuleAssignment assignment = findAssignment(assignmentId);
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId(assignment.getAcademicLevel()),
            PermissionCode.ACADEMIC_RULE_ASSIGNMENT_VIEW
        );
        return toResponse(assignment);
    }

    @Transactional
    public AcademicLevelRuleAssignmentResponse updateAssignment(
        AuthenticatedUserPrincipal principal,
        UUID assignmentId,
        UpdateAcademicLevelRuleAssignmentRequest request
    ) {
        AcademicLevelRuleAssignment assignment = findAssignment(assignmentId);
        UUID establishmentId = establishmentId(assignment.getAcademicLevel());
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.ACADEMIC_RULE_ASSIGNMENT_CREATE
        );
        AcademicRuleProfile profile = findAcademicRuleProfile(
            request.academicRuleProfileId()
        );
        ensureSameEstablishment(
            establishmentId,
            assignment.getAcademicYear(),
            profile
        );
        if (profile.getStatus() != AcademicRuleProfileStatus.ACTIVE) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Only an active academic rule profile can be assigned"
            );
        }
        ensureTerminalLevelCompatibility(assignment.getAcademicLevel(), profile);
        assignment.setAcademicRuleProfile(profile);
        return toResponse(assignmentRepository.save(assignment));
    }

    private AcademicLevelRuleAssignmentResponse createAssignment(
        AuthenticatedUserPrincipal principal,
        AcademicLevel academicLevel,
        UUID academicYearId,
        UUID academicRuleProfileId
    ) {
        UUID establishmentId = establishmentId(academicLevel);
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.ACADEMIC_RULE_ASSIGNMENT_CREATE
        );
        AcademicYear academicYear = findAcademicYear(academicYearId);
        AcademicRuleProfile profile = findAcademicRuleProfile(academicRuleProfileId);
        ensureSameEstablishment(establishmentId, academicYear, profile);
        if (profile.getStatus() != AcademicRuleProfileStatus.ACTIVE) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Only an active academic rule profile can be assigned"
            );
        }
        ensureTerminalLevelCompatibility(academicLevel, profile);
        if (assignmentRepository.existsByAcademicLevelIdAndAcademicYearId(
            academicLevel.getId(),
            academicYearId
        )) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The academic level already has a rule assignment for this academic year"
            );
        }

        AcademicLevelRuleAssignment assignment = new AcademicLevelRuleAssignment();
        assignment.setAcademicLevel(academicLevel);
        assignment.setAcademicYear(academicYear);
        assignment.setAcademicRuleProfile(profile);
        assignment.setStatus(AcademicLevelRuleAssignmentStatus.ACTIVE);
        return toResponse(assignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public void ensureTerminalLevelCompatibility(AcademicLevel academicLevel) {
        if (!academicLevel.isTerminalLevel()) {
            return;
        }
        assignmentRepository
            .findByAcademicLevelIdOrderByAcademicYearStartYearDesc(academicLevel.getId())
            .stream()
            .filter(assignment -> assignment.getStatus() == AcademicLevelRuleAssignmentStatus.ACTIVE)
            .forEach(assignment -> ensureTerminalLevelCompatibility(
                academicLevel,
                assignment.getAcademicRuleProfile()
            ));
    }

    private void ensureTerminalLevelCompatibility(
        AcademicLevel academicLevel,
        AcademicRuleProfile profile
    ) {
        if (academicLevel.isTerminalLevel() && profile.isAllowProgressionWithDebt()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A final academic level cannot use a rule profile that allows progression with debt"
            );
        }
    }

    private AcademicLevel findAcademicLevel(UUID academicLevelId) {
        return academicLevelRepository.findById(academicLevelId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic level not found"
            ));
    }

    private AcademicYear findAcademicYear(UUID academicYearId) {
        return academicYearRepository.findById(academicYearId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic year not found"
            ));
    }

    private AcademicRuleProfile findAcademicRuleProfile(UUID academicRuleProfileId) {
        return academicRuleProfileRepository.findById(academicRuleProfileId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic rule profile not found"
            ));
    }

    private AcademicLevelRuleAssignment findAssignment(UUID assignmentId) {
        return assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic level rule assignment not found"
            ));
    }

    private void ensureSameEstablishment(
        UUID establishmentId,
        AcademicYear academicYear,
        AcademicRuleProfile profile
    ) {
        if (!establishmentId.equals(academicYear.getEstablishment().getId())
            || !establishmentId.equals(profile.getEstablishment().getId())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Academic level, academic year, and rule profile must belong to the same establishment"
            );
        }
    }

    private UUID establishmentId(AcademicLevel academicLevel) {
        return academicLevel
            .getProgramFiliere()
            .getDepartment()
            .getEstablishment()
            .getId();
    }

    private AcademicLevelRuleAssignmentResponse toResponse(
        AcademicLevelRuleAssignment assignment
    ) {
        return new AcademicLevelRuleAssignmentResponse(
            assignment.getId(),
            assignment.getAcademicLevel().getId(),
            assignment.getAcademicYear().getId(),
            assignment.getAcademicRuleProfile().getId(),
            assignment.getStatus(),
            assignment.getCreatedAt(),
            assignment.getUpdatedAt()
        );
    }
}
