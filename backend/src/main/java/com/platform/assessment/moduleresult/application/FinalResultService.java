package com.platform.assessment.moduleresult.application;

import com.platform.academicregistration.moduleregistration.domain.ModuleRegistration;
import com.platform.academicregistration.moduleregistration.domain.ModuleRegistrationStatus;
import com.platform.academicregistration.moduleregistration.infrastructure.ModuleRegistrationRepository;
import com.platform.assessment.moduleresult.domain.ModuleResult;
import com.platform.assessment.moduleresult.infrastructure.ModuleResultRepository;
import com.platform.assessment.moduleresult.presentation.dto.FinalResultResponse;
import com.platform.assessment.progressiondecision.infrastructure.ProgressionDecisionRepository;
import com.platform.assessment.semesterresult.infrastructure.SemesterResultRepository;
import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.UserProfile;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.moduleclassresponsibility.domain.ModuleClassResponsibilityStatus;
import com.platform.moduleclassresponsibility.infrastructure.ModuleClassResponsibilityRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FinalResultService {

    private final ModuleRegistrationRepository registrationRepository;
    private final ModuleResultRepository resultRepository;
    private final ModuleResultService moduleResultService;
    private final AdminPermissionAuthorizationService authorizationService;
    private final ModuleClassResponsibilityRepository responsibilityRepository;
    private final UserProfileRepository profileRepository;
    private final SemesterResultRepository semesterResultRepository;
    private final ProgressionDecisionRepository progressionDecisionRepository;

    public FinalResultService(
        ModuleRegistrationRepository registrationRepository,
        ModuleResultRepository resultRepository,
        ModuleResultService moduleResultService,
        AdminPermissionAuthorizationService authorizationService,
        ModuleClassResponsibilityRepository responsibilityRepository,
        UserProfileRepository profileRepository,
        SemesterResultRepository semesterResultRepository,
        ProgressionDecisionRepository progressionDecisionRepository
    ) {
        this.registrationRepository = registrationRepository;
        this.resultRepository = resultRepository;
        this.moduleResultService = moduleResultService;
        this.authorizationService = authorizationService;
        this.responsibilityRepository = responsibilityRepository;
        this.profileRepository = profileRepository;
        this.semesterResultRepository = semesterResultRepository;
        this.progressionDecisionRepository = progressionDecisionRepository;
    }

    @Transactional
    public List<FinalResultResponse> generate(
        AuthenticatedUserPrincipal principal,
        UUID semesterId,
        UUID classGroupId
    ) {
        List<ModuleRegistration> registrations = registrations(semesterId, classGroupId);
        authorizationService.requirePermission(
            principal,
            establishmentId(registrations),
            PermissionCode.GRADE_PUBLISH
        );
        registrations.forEach(moduleResultService::recalculate);
        return responses(registrations, null);
    }

    @Transactional
    public void clear(
        AuthenticatedUserPrincipal principal,
        UUID semesterId,
        UUID classGroupId
    ) {
        List<ModuleRegistration> registrations = registrations(
            semesterId,
            classGroupId
        );
        authorizationService.requirePermission(
            principal,
            establishmentId(registrations),
            PermissionCode.GRADE_PUBLISH
        );
        List<UUID> semesterRegistrationIds = registrations.stream()
            .map(item -> item.getSemesterRegistration().getId())
            .distinct()
            .toList();
        List<UUID> academicRegistrationIds = registrations.stream()
            .map(item -> item.getSemesterRegistration()
                .getAcademicRegistration().getId())
            .distinct()
            .toList();

        progressionDecisionRepository.deleteByAcademicRegistrationIdIn(
            academicRegistrationIds
        );
        semesterResultRepository.deleteAll(
            semesterResultRepository.findBySemesterRegistrationIdIn(
                semesterRegistrationIds
            )
        );
        resultRepository.deleteAll(
            resultRepository.findByModuleRegistrationIdIn(
                registrations.stream().map(ModuleRegistration::getId).toList()
            )
        );
    }

    @Transactional(readOnly = true)
    public List<FinalResultResponse> get(
        AuthenticatedUserPrincipal principal,
        UUID semesterId,
        UUID classGroupId,
        UUID subjectModuleId
    ) {
        List<ModuleRegistration> registrations = registrations(semesterId, classGroupId);
        if (principal != null && principal.role() == AccountRoleType.PROFESSOR) {
            requireProfessorResponsibility(principal, registrations, classGroupId, subjectModuleId);
        } else {
            authorizationService.requirePermission(
                principal,
                establishmentId(registrations),
                PermissionCode.GRADE_VIEW
            );
        }
        return responses(registrations, subjectModuleId);
    }

    private void requireProfessorResponsibility(
        AuthenticatedUserPrincipal principal,
        List<ModuleRegistration> registrations,
        UUID classGroupId,
        UUID subjectModuleId
    ) {
        ModuleRegistration context = registrations.get(0);
        if (subjectModuleId == null || responsibilityRepository
            .findByProfessorIdAndSubjectModuleIdAndClassGroupIdAndAcademicYearIdAndSemesterId(
                principal.roleEntityId(), subjectModuleId, classGroupId,
                context.getSemesterRegistration().getAcademicRegistration().getAcademicYear().getId(),
                context.getSemesterRegistration().getSemester().getId()
            )
            .filter(item -> item.getStatus() == ModuleClassResponsibilityStatus.ACTIVE)
            .isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Module responsibility required");
        }
    }

    private List<ModuleRegistration> registrations(UUID semesterId, UUID classGroupId) {
        List<ModuleRegistration> registrations = registrationRepository
            .findBySemesterAndClassGroup(semesterId, classGroupId, ModuleRegistrationStatus.ACTIVE);
        if (registrations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No module registrations found for this class");
        }
        return registrations;
    }

    private UUID establishmentId(List<ModuleRegistration> registrations) {
        return registrations.get(0).getSemesterRegistration().getAcademicRegistration()
            .getStudent().getEstablishment().getId();
    }

    private List<FinalResultResponse> responses(
        List<ModuleRegistration> registrations,
        UUID subjectModuleId
    ) {
        return registrations.stream()
            .filter(item -> subjectModuleId == null || item.getSubjectModule().getId().equals(subjectModuleId))
            .map(registration -> response(registration, resultRepository
                .findByModuleRegistrationId(registration.getId()).orElse(null)))
            .toList();
    }

    private FinalResultResponse response(ModuleRegistration registration, ModuleResult result) {
        var student = registration.getSemesterRegistration().getAcademicRegistration().getStudent();
        UserProfile profile = profileRepository.findByUserAccountId(student.getUserAccount().getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student profile not found"));
        return new FinalResultResponse(
            registration.getId(), student.getId(), profile.getFirstName(), profile.getLastName(),
            student.getApogeeCode(), registration.getSubjectModule().getId(),
            registration.getSubjectModule().getCode(), registration.getSubjectModule().getTitle(),
            registration.getInscriptionNumber(), result == null ? null : result.getFinalGradeValue(),
            result == null ? null : result.getResultStatus()
        );
    }
}
