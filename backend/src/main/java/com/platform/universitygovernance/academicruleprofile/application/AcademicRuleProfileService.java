package com.platform.universitygovernance.academicruleprofile.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfile;
import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfileStatus;
import com.platform.universitygovernance.academicruleprofile.domain.AbsenceExclusionPolicy;
import com.platform.universitygovernance.academicruleprofile.domain.SessionGradePolicy;
import com.platform.universitygovernance.academicruleprofile.infrastructure.AcademicRuleProfileRepository;
import com.platform.universitygovernance.academicruleprofile.presentation.dto.AcademicRuleProfileResponse;
import com.platform.universitygovernance.academicruleprofile.presentation.dto.CreateAcademicRuleProfileRequest;
import com.platform.universitygovernance.academicruleprofile.presentation.dto.UpdateAcademicRuleProfileRequest;
import com.platform.universitygovernance.academiclevelruleassignment.infrastructure.AcademicLevelRuleAssignmentRepository;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AcademicRuleProfileService {

    private static final BigDecimal MINIMUM_GRADE = new BigDecimal("0.00");
    private static final BigDecimal MAXIMUM_GRADE = new BigDecimal("20.00");

    private final AcademicRuleProfileRepository academicRuleProfileRepository;
    private final EstablishmentRepository establishmentRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;
    private final AcademicLevelRuleAssignmentRepository ruleAssignmentRepository;

    public AcademicRuleProfileService(
        AcademicRuleProfileRepository academicRuleProfileRepository,
        EstablishmentRepository establishmentRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService,
        AcademicLevelRuleAssignmentRepository ruleAssignmentRepository
    ) {
        this.academicRuleProfileRepository = academicRuleProfileRepository;
        this.establishmentRepository = establishmentRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
        this.ruleAssignmentRepository = ruleAssignmentRepository;
    }

    @Transactional
    public AcademicRuleProfileResponse createAcademicRuleProfile(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        CreateAcademicRuleProfileRequest request
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.ACADEMIC_RULE_PROFILE_CREATE
        );
        Establishment establishment = findEstablishment(establishmentId);
        validateRules(
            request.moduleValidationThreshold(),
            request.compensationMinimumThreshold(),
            request.semesterValidationAverage(),
            request.annualValidationAverage(),
            request.maximumModuleInscriptions(),
            request.sessionGradePolicy(),
            request.allowProgressionWithDebt(),
            request.maximumCarriedModules(),
            request.maximumUnjustifiedAbsences(),
            request.absenceExclusionPolicy(),
            request.status()
        );

        String name = normalizeName(request.name());
        Optional<AcademicRuleProfile> latestProfile = academicRuleProfileRepository
            .findTopByEstablishmentIdAndNameIgnoreCaseOrderByVersionDesc(establishmentId, name);
        int version = latestProfile.map(profile -> profile.getVersion() + 1).orElse(1);
        String profileName = latestProfile.map(AcademicRuleProfile::getName).orElse(name);

        AcademicRuleProfile profile = new AcademicRuleProfile();
        profile.setEstablishment(establishment);
        profile.setName(profileName);
        profile.setVersion(version);
        applyRules(profile, request);
        return toResponse(academicRuleProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public List<AcademicRuleProfileResponse> getAcademicRuleProfiles(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.ACADEMIC_RULE_PROFILE_VIEW
        );
        findEstablishment(establishmentId);
        return academicRuleProfileRepository
            .findByEstablishmentOrderByNameAndVersion(establishmentId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public AcademicRuleProfileResponse getAcademicRuleProfile(
        AuthenticatedUserPrincipal principal,
        UUID academicRuleProfileId
    ) {
        AcademicRuleProfile profile = findAcademicRuleProfile(academicRuleProfileId);
        permissionAuthorizationService.requirePermission(
            principal,
            profile.getEstablishment().getId(),
            PermissionCode.ACADEMIC_RULE_PROFILE_VIEW
        );
        return toResponse(profile);
    }

    @Transactional
    public AcademicRuleProfileResponse updateAcademicRuleProfile(
        AuthenticatedUserPrincipal principal,
        UUID academicRuleProfileId,
        UpdateAcademicRuleProfileRequest request
    ) {
        AcademicRuleProfile profile = findAcademicRuleProfile(academicRuleProfileId);
        UUID establishmentId = profile.getEstablishment().getId();
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.ACADEMIC_RULE_PROFILE_UPDATE
        );
        validateRules(
            request.moduleValidationThreshold(),
            request.compensationMinimumThreshold(),
            request.semesterValidationAverage(),
            request.annualValidationAverage(),
            request.maximumModuleInscriptions(),
            request.sessionGradePolicy(),
            request.allowProgressionWithDebt(),
            request.maximumCarriedModules(),
            request.maximumUnjustifiedAbsences(),
            request.absenceExclusionPolicy(),
            request.status()
        );

        String name = normalizeName(request.name());
        if (ruleAssignmentRepository.existsByAcademicRuleProfileId(academicRuleProfileId)
            && isRuleDefinitionChanged(profile, name, request)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "An assigned academic rule profile is immutable; create a new version instead"
            );
        }
        if (academicRuleProfileRepository
            .existsByEstablishmentIdAndNameIgnoreCaseAndVersionAndIdNot(
                establishmentId,
                name,
                profile.getVersion(),
                academicRuleProfileId
            )) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "An academic rule profile with this name and version already exists"
            );
        }

        profile.setName(name);
        applyRules(profile, request);
        return toResponse(academicRuleProfileRepository.save(profile));
    }

    private Establishment findEstablishment(UUID establishmentId) {
        return establishmentRepository.findById(establishmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Establishment not found"
            ));
    }

    private AcademicRuleProfile findAcademicRuleProfile(UUID academicRuleProfileId) {
        return academicRuleProfileRepository.findById(academicRuleProfileId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic rule profile not found"
            ));
    }

    private void validateRules(
        BigDecimal moduleValidationThreshold,
        BigDecimal compensationMinimumThreshold,
        BigDecimal semesterValidationAverage,
        BigDecimal annualValidationAverage,
        Integer maximumModuleInscriptions,
        SessionGradePolicy sessionGradePolicy,
        Boolean allowProgressionWithDebt,
        Integer maximumCarriedModules,
        Integer maximumUnjustifiedAbsences,
        AbsenceExclusionPolicy absenceExclusionPolicy,
        AcademicRuleProfileStatus status
    ) {
        requireGrade(moduleValidationThreshold, "Module validation threshold");
        requireGrade(compensationMinimumThreshold, "Compensation minimum threshold");
        requireGrade(semesterValidationAverage, "Semester validation average");
        if (annualValidationAverage != null) {
            requireGrade(annualValidationAverage, "Annual validation average");
        }
        if (compensationMinimumThreshold.compareTo(moduleValidationThreshold) > 0) {
            throw badRequest("Compensation minimum threshold cannot exceed module validation threshold");
        }
        if (maximumModuleInscriptions == null || maximumModuleInscriptions <= 0) {
            throw badRequest("Maximum module inscriptions must be positive");
        }
        if (sessionGradePolicy == null) {
            throw badRequest("Session grade policy is required");
        }
        if (allowProgressionWithDebt == null) {
            throw badRequest("Progression-with-debt setting is required");
        }
        if (maximumCarriedModules == null || maximumCarriedModules < 0) {
            throw badRequest("Maximum carried modules cannot be negative");
        }
        if (allowProgressionWithDebt && maximumCarriedModules == 0) {
            throw badRequest("Maximum carried modules must be positive when progression with debt is allowed");
        }
        if (!allowProgressionWithDebt && maximumCarriedModules != 0) {
            throw badRequest("Maximum carried modules must be zero when progression with debt is disabled");
        }
        if (maximumUnjustifiedAbsences == null || maximumUnjustifiedAbsences < 0) {
            throw badRequest("Maximum unjustified absences cannot be negative");
        }
        if (absenceExclusionPolicy == null) {
            throw badRequest("Absence exclusion policy is required");
        }
        if (status == null) {
            throw badRequest("Academic rule profile status is required");
        }
    }

    private void requireGrade(BigDecimal value, String fieldName) {
        if (value == null
            || value.compareTo(MINIMUM_GRADE) < 0
            || value.compareTo(MAXIMUM_GRADE) > 0) {
            throw badRequest(fieldName + " must be between 0.00 and 20.00");
        }
    }

    private void applyRules(
        AcademicRuleProfile profile,
        CreateAcademicRuleProfileRequest request
    ) {
        profile.setModuleValidationThreshold(request.moduleValidationThreshold());
        profile.setCompensationMinimumThreshold(request.compensationMinimumThreshold());
        profile.setSemesterValidationAverage(request.semesterValidationAverage());
        profile.setAnnualValidationAverage(request.annualValidationAverage());
        profile.setMaximumModuleInscriptions(request.maximumModuleInscriptions());
        profile.setSessionGradePolicy(request.sessionGradePolicy());
        profile.setAllowProgressionWithDebt(request.allowProgressionWithDebt());
        profile.setMaximumCarriedModules(request.maximumCarriedModules());
        profile.setMaximumUnjustifiedAbsences(request.maximumUnjustifiedAbsences());
        profile.setAbsenceExclusionPolicy(request.absenceExclusionPolicy());
        profile.setStatus(request.status());
    }

    private boolean isRuleDefinitionChanged(
        AcademicRuleProfile profile,
        String name,
        UpdateAcademicRuleProfileRequest request
    ) {
        return !profile.getName().equals(name)
            || profile.getModuleValidationThreshold().compareTo(
                request.moduleValidationThreshold()
            ) != 0
            || profile.getCompensationMinimumThreshold().compareTo(
                request.compensationMinimumThreshold()
            ) != 0
            || profile.getSemesterValidationAverage().compareTo(
                request.semesterValidationAverage()
            ) != 0
            || !sameGrade(
                profile.getAnnualValidationAverage(),
                request.annualValidationAverage()
            )
            || profile.getMaximumModuleInscriptions()
                != request.maximumModuleInscriptions()
            || profile.getSessionGradePolicy() != request.sessionGradePolicy()
            || profile.isAllowProgressionWithDebt()
                != request.allowProgressionWithDebt()
            || profile.getMaximumCarriedModules() != request.maximumCarriedModules()
            || profile.getMaximumUnjustifiedAbsences()
                != request.maximumUnjustifiedAbsences()
            || profile.getAbsenceExclusionPolicy()
                != request.absenceExclusionPolicy();
    }

    private boolean sameGrade(BigDecimal first, BigDecimal second) {
        if (first == null || second == null) {
            return first == second;
        }
        return first.compareTo(second) == 0;
    }

    private void applyRules(
        AcademicRuleProfile profile,
        UpdateAcademicRuleProfileRequest request
    ) {
        profile.setModuleValidationThreshold(request.moduleValidationThreshold());
        profile.setCompensationMinimumThreshold(request.compensationMinimumThreshold());
        profile.setSemesterValidationAverage(request.semesterValidationAverage());
        profile.setAnnualValidationAverage(request.annualValidationAverage());
        profile.setMaximumModuleInscriptions(request.maximumModuleInscriptions());
        profile.setSessionGradePolicy(request.sessionGradePolicy());
        profile.setAllowProgressionWithDebt(request.allowProgressionWithDebt());
        profile.setMaximumCarriedModules(request.maximumCarriedModules());
        profile.setMaximumUnjustifiedAbsences(request.maximumUnjustifiedAbsences());
        profile.setAbsenceExclusionPolicy(request.absenceExclusionPolicy());
        profile.setStatus(request.status());
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw badRequest("Academic rule profile name is required");
        }
        return name.trim();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private AcademicRuleProfileResponse toResponse(AcademicRuleProfile profile) {
        return new AcademicRuleProfileResponse(
            profile.getId(),
            profile.getEstablishment().getId(),
            profile.getName(),
            profile.getVersion(),
            profile.getModuleValidationThreshold(),
            profile.getCompensationMinimumThreshold(),
            profile.getSemesterValidationAverage(),
            profile.getAnnualValidationAverage(),
            profile.getMaximumModuleInscriptions(),
            profile.getSessionGradePolicy(),
            profile.isAllowProgressionWithDebt(),
            profile.getMaximumCarriedModules(),
            profile.getMaximumUnjustifiedAbsences(),
            profile.getAbsenceExclusionPolicy(),
            profile.getStatus(),
            profile.getCreatedAt(),
            profile.getUpdatedAt()
        );
    }
}
