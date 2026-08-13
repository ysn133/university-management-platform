package com.platform.assessment.semesterresult.application;

import com.platform.academicregistration.moduleregistration.domain.ModuleRegistration;
import com.platform.academicregistration.moduleregistration.domain.ModuleRegistrationStatus;
import com.platform.academicregistration.moduleregistration.infrastructure.ModuleRegistrationRepository;
import com.platform.academicregistration.classassignment.infrastructure.StudentClassAssignmentRepository;
import com.platform.assessment.moduleresult.application.FinalResultService;
import com.platform.assessment.moduleresult.domain.ModuleResult;
import com.platform.assessment.moduleresult.domain.ModuleResultStatus;
import com.platform.assessment.moduleresult.infrastructure.ModuleResultRepository;
import com.platform.assessment.semesterresult.domain.SemesterResult;
import com.platform.assessment.semesterresult.infrastructure.SemesterResultRepository;
import com.platform.assessment.semesterresult.presentation.dto.ManagedSemesterResultResponse;
import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.UserProfile;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ManagedSemesterResultService {

    private final ModuleRegistrationRepository registrationRepository;
    private final ModuleResultRepository moduleResultRepository;
    private final SemesterResultRepository semesterResultRepository;
    private final FinalResultService finalResultService;
    private final AdminPermissionAuthorizationService authorizationService;
    private final UserProfileRepository profileRepository;
    private final StudentClassAssignmentRepository classAssignmentRepository;

    public ManagedSemesterResultService(
        ModuleRegistrationRepository registrationRepository,
        ModuleResultRepository moduleResultRepository,
        SemesterResultRepository semesterResultRepository,
        FinalResultService finalResultService,
        AdminPermissionAuthorizationService authorizationService,
        UserProfileRepository profileRepository,
        StudentClassAssignmentRepository classAssignmentRepository
    ) {
        this.registrationRepository = registrationRepository;
        this.moduleResultRepository = moduleResultRepository;
        this.semesterResultRepository = semesterResultRepository;
        this.finalResultService = finalResultService;
        this.authorizationService = authorizationService;
        this.profileRepository = profileRepository;
        this.classAssignmentRepository = classAssignmentRepository;
    }

    @Transactional
    public List<ManagedSemesterResultResponse> generate(AuthenticatedUserPrincipal principal, UUID semesterId, UUID classGroupId) {
        finalResultService.generate(principal, semesterId, classGroupId);
        return results(principal, semesterId, classGroupId);
    }

    @Transactional(readOnly = true)
    public List<ManagedSemesterResultResponse> get(AuthenticatedUserPrincipal principal, UUID semesterId, UUID classGroupId) {
        return results(principal, semesterId, classGroupId);
    }

    private List<ManagedSemesterResultResponse> results(AuthenticatedUserPrincipal principal, UUID semesterId, UUID classGroupId) {
        List<ModuleRegistration> registrations = registrationRepository.findBySemesterAndClassGroup(
            semesterId, classGroupId, ModuleRegistrationStatus.ACTIVE
        );
        if (registrations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No module registrations found for this class");
        }
        UUID establishmentId = registrations.get(0).getSemesterRegistration().getAcademicRegistration().getStudent().getEstablishment().getId();
        authorizationService.requirePermission(principal, establishmentId, PermissionCode.GRADE_VIEW);

        Map<UUID, ModuleRegistration> registrationBySemesterRegistration = registrations.stream().collect(Collectors.toMap(
            item -> item.getSemesterRegistration().getId(), Function.identity(), (first, ignored) -> first
        ));
        Map<UUID, List<ModuleResult>> moduleResults = moduleResultRepository
            .findByModuleRegistrationSemesterRegistrationIdIn(registrationBySemesterRegistration.keySet().stream().toList())
            .stream().collect(Collectors.groupingBy(item -> item.getModuleRegistration().getSemesterRegistration().getId()));

        return semesterResultRepository.findBySemesterRegistrationIdIn(registrationBySemesterRegistration.keySet().stream().toList())
            .stream().map(result -> response(result, registrationBySemesterRegistration.get(result.getSemesterRegistration().getId()), moduleResults.getOrDefault(result.getSemesterRegistration().getId(), List.of())))
            .toList();
    }

    private boolean isSecondInscriptionOnly(List<ModuleResult> moduleResults) {
        return !moduleResults.isEmpty() && moduleResults.stream()
            .allMatch(item -> item.getModuleRegistration().getInscriptionNumber() > 1);
    }

    private ManagedSemesterResultResponse response(SemesterResult result, ModuleRegistration registration, List<ModuleResult> moduleResults) {
        var student = registration.getSemesterRegistration().getAcademicRegistration().getStudent();
        boolean secondInscriptionOnly = isSecondInscriptionOnly(moduleResults);
        ModuleRegistration originalRegistration = secondInscriptionOnly
            ? moduleResults.stream()
                .map(ModuleResult::getModuleRegistration)
                .map(item -> registrationRepository.findEarlierInscription(
                    student.getId(), item.getSubjectModule().getCode(), item.getInscriptionNumber()
                ).stream().findFirst().orElse(null))
                .filter(java.util.Objects::nonNull)
                .findFirst().orElse(null)
            : null;
        UserProfile profile = profileRepository.findByUserAccountId(student.getUserAccount().getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student profile not found"));
        var originalSemesterRegistration = originalRegistration == null ? null : originalRegistration.getSemesterRegistration();
        UUID originalClassGroupId = originalSemesterRegistration == null ? null : classAssignmentRepository
            .findBySemesterRegistrationId(originalSemesterRegistration.getId())
            .map(assignment -> assignment.getClassGroup().getId())
            .orElse(null);
        return new ManagedSemesterResultResponse(
            result.getId(), result.getSemesterRegistration().getId(), student.getId(), profile.getFirstName(), profile.getLastName(), student.getApogeeCode(),
            result.getSemesterAverage(), result.getResultStatus(), count(moduleResults, ModuleResultStatus.V), count(moduleResults, ModuleResultStatus.AV),
            count(moduleResults, ModuleResultStatus.NV), result.getEvaluatedAt(), secondInscriptionOnly,
            originalSemesterRegistration == null ? null : originalSemesterRegistration.getAcademicRegistration().getAcademicYear().getId(),
            originalSemesterRegistration == null ? null : originalSemesterRegistration.getSemester().getAcademicLevel().getId(),
            originalSemesterRegistration == null ? null : originalSemesterRegistration.getSemester().getId(),
            originalClassGroupId,
            originalSemesterRegistration == null ? null : originalSemesterRegistration.getAcademicRegistration().getAcademicYear().getLabel(),
            originalSemesterRegistration == null ? null : originalSemesterRegistration.getSemester().getAcademicLevel().getName(),
            originalSemesterRegistration == null ? null : originalSemesterRegistration.getSemester().getName()
        );
    }

    private long count(List<ModuleResult> results, ModuleResultStatus status) {
        return results.stream().filter(item -> item.getResultStatus() == status).count();
    }
}
