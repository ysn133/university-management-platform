package com.platform.usermanagement.professor.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.Professor;
import com.platform.identityaccess.domain.UserAccount;
import com.platform.identityaccess.domain.UserProfile;
import com.platform.identityaccess.infrastructure.ProfessorRepository;
import com.platform.identityaccess.infrastructure.UserAccountRepository;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.usermanagement.professor.presentation.dto.CreateProfessorRequest;
import com.platform.usermanagement.professor.presentation.dto.CreateProfessorResponse;
import com.platform.usermanagement.professor.presentation.dto.ProfessorProfileResponse;
import com.platform.usermanagement.professor.presentation.dto.UpdateProfessorRequest;
import com.platform.usermanagement.professor.expertise.infrastructure.ProfessorExpertiseRepository;
import com.platform.usermanagement.shared.presentation.dto.ResetManagedPasswordRequest;
import com.platform.shared.presentation.ActionResponse;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfessorManagementService {

    private final EstablishmentRepository establishmentRepository;
    private final ProfessorRepository professorRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;
    private final ProfessorExpertiseRepository professorExpertiseRepository;

    public ProfessorManagementService(
        EstablishmentRepository establishmentRepository,
        ProfessorRepository professorRepository,
        UserAccountRepository userAccountRepository,
        UserProfileRepository userProfileRepository,
        PasswordEncoder passwordEncoder,
        AdminPermissionAuthorizationService permissionAuthorizationService,
        ProfessorExpertiseRepository professorExpertiseRepository
    ) {
        this.establishmentRepository = establishmentRepository;
        this.professorRepository = professorRepository;
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.permissionAuthorizationService = permissionAuthorizationService;
        this.professorExpertiseRepository = professorExpertiseRepository;
    }

    @Transactional
    public CreateProfessorResponse createProfessor(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        CreateProfessorRequest request
    ) {
        requirePermission(principal, establishmentId, PermissionCode.PROFESSOR_CREATE);
        Establishment establishment = findEstablishment(establishmentId);

        if (request.hireDate() != null && request.hireDate().isBefore(request.birthDate())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Hire date cannot be before birth date"
            );
        }

        String universityEmail = request.universityEmail().trim().toLowerCase(Locale.ROOT);
        if (userAccountRepository.existsByUniversityEmail(universityEmail)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "University email already exists"
            );
        }

        String employeeNumber = request.employeeNumber().trim().toUpperCase(Locale.ROOT);
        if (professorRepository.existsByEmployeeNumberIgnoreCase(employeeNumber)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Employee number already exists"
            );
        }

        String cin = normalizeOptionalUppercase(request.cin());
        if (cin != null && userProfileRepository.existsByCinIgnoreCase(cin)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CIN already exists");
        }

        UserAccount account = new UserAccount();
        account.setUniversityEmail(universityEmail);
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setRole(AccountRoleType.PROFESSOR);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account = userAccountRepository.save(account);

        UserProfile profile = new UserProfile();
        profile.setUserAccount(account);
        profile.setFirstName(request.firstName().trim());
        profile.setLastName(request.lastName().trim());
        profile.setBirthDate(request.birthDate());
        profile.setPlaceOfBirth(request.placeOfBirth().trim());
        profile.setNationality(request.nationality().trim());
        profile.setCin(cin);
        profile.setSex(request.sex());
        profile.setPhoneNumber(normalizeOptional(request.phoneNumber()));
        userProfileRepository.save(profile);

        Professor professor = new Professor();
        professor.setUserAccount(account);
        professor.setEstablishment(establishment);
        professor.setEmployeeNumber(employeeNumber);
        professor.setAcademicRank(normalizeOptional(request.academicRank()));
        professor.setHireDate(request.hireDate());
        professor.setMaximumWeeklyTeachingMinutes(request.maximumWeeklyTeachingMinutes());
        professor = professorRepository.save(professor);

        return new CreateProfessorResponse(
            professor.getId(),
            account.getId(),
            establishmentId,
            professor.getEmployeeNumber(),
            account.getRole()
        );
    }

    @Transactional(readOnly = true)
    public List<ProfessorProfileResponse> getProfessors(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        return getProfessors(principal, establishmentId, null, null, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<ProfessorProfileResponse> getProfessors(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        String query,
        AccountStatus status,
        LocalDate joinedFrom,
        LocalDate joinedTo,
        UUID academicDomainId
    ) {
        requirePermission(principal, establishmentId, PermissionCode.PROFESSOR_VIEW);
        findEstablishment(establishmentId);

        String normalizedQuery = normalizeQuery(query);
        return professorRepository.findByEstablishmentIdOrderByCreatedAtAsc(establishmentId).stream()
            .filter(professor -> status == null || professor.getUserAccount().getAccountStatus() == status)
            .filter(professor -> joinedFrom == null || professor.getHireDate() != null
                && !professor.getHireDate().isBefore(joinedFrom))
            .filter(professor -> joinedTo == null || professor.getHireDate() != null
                && !professor.getHireDate().isAfter(joinedTo))
            .filter(professor -> academicDomainId == null
                || professorExpertiseRepository.existsByProfessorIdAndAcademicDomainId(professor.getId(), academicDomainId))
            .filter(professor -> normalizedQuery == null
                || matchesQuery(professor, findProfile(professor), normalizedQuery))
            .map(this::toResponse)
            .sorted(Comparator.comparing(ProfessorProfileResponse::lastName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(ProfessorProfileResponse::firstName, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    @Transactional(readOnly = true)
    public ProfessorProfileResponse getProfessor(
        AuthenticatedUserPrincipal principal,
        UUID professorId
    ) {
        Professor professor = findProfessor(professorId);
        requirePermission(
            principal,
            professor.getEstablishment().getId(),
            PermissionCode.PROFESSOR_VIEW
        );
        return toResponse(professor);
    }

    @Transactional
    public ProfessorProfileResponse updateProfessor(AuthenticatedUserPrincipal principal, UUID professorId,
        UpdateProfessorRequest request) {
        Professor professor = findProfessor(professorId);
        requirePermission(principal, professor.getEstablishment().getId(), PermissionCode.PROFESSOR_UPDATE);
        if (request.hireDate() != null && request.hireDate().isBefore(request.birthDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hire date cannot be before birth date");
        }

        UserAccount account = professor.getUserAccount();
        UserProfile profile = findProfile(professor);
        String email = request.universityEmail().trim().toLowerCase(Locale.ROOT);
        String employeeNumber = request.employeeNumber().trim().toUpperCase(Locale.ROOT);
        String cin = normalizeOptionalUppercase(request.cin());
        if (userAccountRepository.existsByUniversityEmailAndIdNot(email, account.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "University email already exists");
        }
        if (professorRepository.existsByEmployeeNumberIgnoreCaseAndIdNot(employeeNumber, professor.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee number already exists");
        }
        if (cin != null && userProfileRepository.existsByCinIgnoreCaseAndIdNot(cin, profile.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CIN already exists");
        }

        account.setUniversityEmail(email);
        professor.setEmployeeNumber(employeeNumber);
        professor.setAcademicRank(normalizeOptional(request.academicRank()));
        professor.setHireDate(request.hireDate());
        professor.setMaximumWeeklyTeachingMinutes(request.maximumWeeklyTeachingMinutes());
        profile.setFirstName(request.firstName().trim());
        profile.setLastName(request.lastName().trim());
        profile.setBirthDate(request.birthDate());
        profile.setPlaceOfBirth(request.placeOfBirth().trim());
        profile.setNationality(request.nationality().trim());
        profile.setCin(cin);
        profile.setSex(request.sex());
        profile.setPhoneNumber(normalizeOptional(request.phoneNumber()));
        return toResponse(professor);
    }

    @Transactional
    public ActionResponse resetPassword(AuthenticatedUserPrincipal principal, UUID professorId,
        ResetManagedPasswordRequest request) {
        UserAccount account = findManagedProfessor(principal, professorId).getUserAccount();
        if (passwordEncoder.matches(request.newPassword(), account.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from the current password");
        }
        account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        return new ActionResponse(true, "Password has been reset");
    }

    @Transactional
    public ActionResponse lockAccount(AuthenticatedUserPrincipal principal, UUID professorId) {
        UserAccount account = findManagedProfessor(principal, professorId).getUserAccount();
        ensureCanLock(account);
        account.setAccountStatus(AccountStatus.LOCKED);
        return new ActionResponse(true, "Account locked");
    }

    @Transactional
    public ActionResponse unlockAccount(AuthenticatedUserPrincipal principal, UUID professorId) {
        UserAccount account = findManagedProfessor(principal, professorId).getUserAccount();
        if (account.getAccountStatus() != AccountStatus.LOCKED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only a locked account can be unlocked");
        }
        account.setAccountStatus(AccountStatus.ACTIVE);
        return new ActionResponse(true, "Account unlocked");
    }

    @Transactional
    public ActionResponse deactivateAccount(AuthenticatedUserPrincipal principal, UUID professorId) {
        UserAccount account = findManagedProfessor(principal, professorId).getUserAccount();
        if (account.getAccountStatus() == AccountStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Archived account cannot be deactivated");
        }
        account.setAccountStatus(AccountStatus.DEACTIVATED);
        return new ActionResponse(true, "Account deactivated");
    }

    @Transactional
    public ActionResponse archiveAccount(AuthenticatedUserPrincipal principal, UUID professorId) {
        findManagedProfessor(principal, professorId).getUserAccount().setAccountStatus(AccountStatus.ARCHIVED);
        return new ActionResponse(true, "Account archived");
    }

    private Establishment findEstablishment(UUID establishmentId) {
        return establishmentRepository.findById(establishmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Establishment not found"
            ));
    }

    private Professor findProfessor(UUID professorId) {
        return professorRepository.findById(professorId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Professor not found"
            ));
    }

    private Professor findManagedProfessor(AuthenticatedUserPrincipal principal, UUID professorId) {
        Professor professor = findProfessor(professorId);
        requirePermission(principal, professor.getEstablishment().getId(), PermissionCode.PROFESSOR_ACCOUNT_MANAGE);
        return professor;
    }

    private UserProfile findProfile(Professor professor) {
        return userProfileRepository.findByUserAccountId(professor.getUserAccount().getId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Professor profile not found"
            ));
    }

    private void requirePermission(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        PermissionCode permissionCode
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            permissionCode
        );
    }

    private ProfessorProfileResponse toResponse(Professor professor) {
        UserAccount account = professor.getUserAccount();
        UserProfile profile = findProfile(professor);
        return new ProfessorProfileResponse(
            professor.getId(),
            account.getId(),
            professor.getEstablishment().getId(),
            professor.getEmployeeNumber(),
            professor.getAcademicRank(),
            professor.getHireDate(),
            professor.getMaximumWeeklyTeachingMinutes(),
            account.getUniversityEmail(),
            account.getRole(),
            account.getAccountStatus(),
            profile.getFirstName(),
            profile.getLastName(),
            profile.getBirthDate(),
            profile.getPlaceOfBirth(),
            profile.getNationality(),
            profile.getCin(),
            profile.getSex(),
            profile.getPhoneNumber(),
            profile.getProfilePicturePath()
        );
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeOptionalUppercase(String value) {
        String normalized = normalizeOptional(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeQuery(String value) {
        String normalized = normalizeOptional(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private boolean matchesQuery(Professor professor, UserProfile profile, String query) {
        String fullName = (profile.getFirstName() + " " + profile.getLastName()).toLowerCase(Locale.ROOT);
        return fullName.contains(query)
            || professor.getUserAccount().getUniversityEmail().toLowerCase(Locale.ROOT).contains(query)
            || professor.getEmployeeNumber().toLowerCase(Locale.ROOT).contains(query)
            || profile.getCin() != null && profile.getCin().toLowerCase(Locale.ROOT).contains(query);
    }

    private void ensureCanLock(UserAccount account) {
        if (account.getAccountStatus() == AccountStatus.DEACTIVATED
            || account.getAccountStatus() == AccountStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inactive account cannot be locked");
        }
    }
}
