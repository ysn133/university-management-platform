package com.platform.usermanagement.superadmin.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.SuperAdmin;
import com.platform.identityaccess.domain.UserAccount;
import com.platform.identityaccess.domain.UserProfile;
import com.platform.identityaccess.infrastructure.SuperAdminRepository;
import com.platform.identityaccess.infrastructure.UserAccountRepository;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.domain.EstablishmentStatus;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.usermanagement.superadmin.presentation.dto.CreateSuperAdminRequest;
import com.platform.usermanagement.superadmin.presentation.dto.CreateSuperAdminResponse;
import com.platform.usermanagement.superadmin.presentation.dto.ResetPasswordRequest;
import com.platform.usermanagement.superadmin.presentation.dto.SuperAdminProfileResponse;
import com.platform.usermanagement.superadmin.presentation.dto.UpdateSuperAdminRequest;

import jakarta.transaction.Transactional;

@Service
public class SuperAdminManagementService {

    private final EstablishmentRepository establishmentRepository;
    private final SuperAdminRepository superAdminRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public SuperAdminManagementService(
        EstablishmentRepository establishmentRepository,
        SuperAdminRepository superAdminRepository,
        UserAccountRepository userAccountRepository,
        UserProfileRepository userProfileRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.establishmentRepository = establishmentRepository;
        this.superAdminRepository = superAdminRepository;
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CreateSuperAdminResponse createSuperAdmin(CreateSuperAdminRequest request, UUID establishmentId) {
        Establishment establishment = establishmentRepository.findById(establishmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Establishment not found"));

        if (establishment.getEstablishmentStatus() != EstablishmentStatus.ACTIVE) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Super admin cannot be created in an inactive establishment"
            );
        }

        String universityEmail = normalizeEmail(request.universityEmail());
        String cin = normalizeOptionalCin(request.cin());

        if (userAccountRepository.existsByUniversityEmail(universityEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "University email already exists");
        }

        if (cin != null && userProfileRepository.existsByCinIgnoreCase(cin)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CIN already exists");
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setUniversityEmail(universityEmail);
        userAccount.setRole(AccountRoleType.SUPER_ADMIN);
        userAccount.setPasswordHash(passwordEncoder.encode(request.password()));
        userAccount.setAccountStatus(AccountStatus.ACTIVE);

        UserAccount createdUserAccount = userAccountRepository.save(userAccount);

        UserProfile userProfile = new UserProfile();
        userProfile.setUserAccount(createdUserAccount);
        userProfile.setFirstName(request.firstName().trim());
        userProfile.setLastName(request.lastName().trim());
        userProfile.setBirthDate(request.birthDate());
        userProfile.setCin(cin);
        userProfile.setSex(request.sex());
        userProfile.setPhoneNumber(normalizeOptional(request.phoneNumber()));
        userProfileRepository.save(userProfile);

        SuperAdmin superAdmin = new SuperAdmin();
        superAdmin.setUserAccount(createdUserAccount);
        superAdmin.setEstablishment(establishment);
        superAdminRepository.save(superAdmin);

        return new CreateSuperAdminResponse(
            createdUserAccount.getId(),
            establishmentId,
            createdUserAccount.getRole()
        );
    }

    @Transactional
    public ActionResponse resetPassword(ResetPasswordRequest request, UUID superAdminId) {
        SuperAdmin superAdmin = findSuperAdmin(superAdminId);
        UserAccount userAccount = findUserAccount(superAdmin);

        if (passwordEncoder.matches(request.newPassword(), userAccount.getPasswordHash())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "New password must be different from the current password"
            );
        }

        userAccount.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userAccountRepository.save(userAccount);

        return new ActionResponse(true, "Password has been reset.");
    }

    @Transactional
    public ActionResponse lockAccount(UUID superAdminId) {
        SuperAdmin superAdmin = findSuperAdmin(superAdminId);
        UserAccount userAccount = findUserAccount(superAdmin);
        ensureAccountCanBeLocked(userAccount);
        userAccount.setAccountStatus(AccountStatus.LOCKED);

        return new ActionResponse(true, "Account locked");
    }

    @Transactional
    public ActionResponse unlockAccount(UUID superAdminId) {
        SuperAdmin superAdmin = findSuperAdmin(superAdminId);
        UserAccount userAccount = findUserAccount(superAdmin);
        ensureAccountCanBeUnlocked(userAccount);
        userAccount.setAccountStatus(AccountStatus.ACTIVE);

        return new ActionResponse(true, "Account unlocked");
    }

    @Transactional
    public ActionResponse deactivateAccount(UUID superAdminId) {
        UserAccount userAccount = findUserAccount(findSuperAdmin(superAdminId));

        if (userAccount.getAccountStatus() == AccountStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Archived account cannot be deactivated");
        }

        userAccount.setAccountStatus(AccountStatus.DEACTIVATED);
        return new ActionResponse(true, "Account deactivated");
    }

    @Transactional
    public ActionResponse archiveAccount(UUID superAdminId) {
        UserAccount userAccount = findUserAccount(findSuperAdmin(superAdminId));
        userAccount.setAccountStatus(AccountStatus.ARCHIVED);
        return new ActionResponse(true, "Account archived");
    }

    @Transactional
    public SuperAdminProfileResponse updateSuperAdmin(
        UUID superAdminId,
        UpdateSuperAdminRequest request
    ) {
        SuperAdmin superAdmin = findSuperAdmin(superAdminId);
        UserAccount userAccount = findUserAccount(superAdmin);
        UserProfile userProfile = findProfile(superAdmin);

        String universityEmail = normalizeEmail(request.universityEmail());
        if (!universityEmail.equalsIgnoreCase(userAccount.getUniversityEmail())
            && userAccountRepository.existsByUniversityEmail(universityEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "University email already exists");
        }

        String cin = normalizeOptionalCin(request.cin());
        if (cin != null
            && !cin.equalsIgnoreCase(userProfile.getCin() == null ? "" : userProfile.getCin())
            && userProfileRepository.existsByCinIgnoreCase(cin)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CIN already exists");
        }

        userAccount.setUniversityEmail(universityEmail);
        userProfile.setFirstName(request.firstName().trim());
        userProfile.setLastName(request.lastName().trim());
        userProfile.setBirthDate(request.birthDate());
        userProfile.setCin(cin);
        userProfile.setSex(request.sex());
        userProfile.setPhoneNumber(normalizeOptional(request.phoneNumber()));

        return toResponse(superAdmin, userProfile);
    }


    @Transactional
    public SuperAdminProfileResponse getSuperAdmin(UUID superAdminId) {

        SuperAdmin superAdmin = findSuperAdmin(superAdminId);

        return toResponse(superAdmin, findProfile(superAdmin));

    }


    @Transactional
    public List<SuperAdminProfileResponse> getSuperAdmins(
        UUID establishmentId,
        String query,
        AccountStatus status
    ) {

        if (!establishmentRepository.existsById(establishmentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Establishment not found");
        }

        List<SuperAdmin> superAdmins = superAdminRepository.findByEstablishmentId(establishmentId);

        List<SuperAdminProfileResponse> responses = new ArrayList<>();
        String normalizedQuery = query == null || query.isBlank()
            ? null
            : query.trim().toLowerCase(Locale.ROOT);

        for (SuperAdmin superAdmin : superAdmins) {
            UserProfile userProfile = findProfile(superAdmin);

            if (status != null && superAdmin.getUserAccount().getAccountStatus() != status) {
                continue;
            }

            if (normalizedQuery != null && !matchesQuery(superAdmin, userProfile, normalizedQuery)) {
                continue;
            }

            responses.add(toResponse(superAdmin, userProfile));
        }

        responses.sort(
            Comparator.comparing(SuperAdminProfileResponse::lastName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(SuperAdminProfileResponse::firstName, String.CASE_INSENSITIVE_ORDER)
        );

        return responses;
    }




    private SuperAdmin findSuperAdmin(UUID superAdminId) {
        return superAdminRepository.findById(superAdminId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Super admin not found"));
    }

    private UserAccount findUserAccount(SuperAdmin superAdmin) {
        return userAccountRepository.findById(superAdmin.getUserAccount().getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found"));
    }

    private UserProfile findProfile(SuperAdmin superAdmin) {
        return userProfileRepository.findByUserAccountId(superAdmin.getUserAccount().getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Super admin profile not found"));
    }

    private SuperAdminProfileResponse toResponse(SuperAdmin superAdmin, UserProfile userProfile) {
        UserAccount userAccount = superAdmin.getUserAccount();
        return new SuperAdminProfileResponse(
            superAdmin.getId(),
            userAccount.getId(),
            superAdmin.getEstablishment().getId(),
            userAccount.getUniversityEmail(),
            userAccount.getRole(),
            userAccount.getAccountStatus(),
            userProfile.getFirstName(),
            userProfile.getLastName(),
            userProfile.getBirthDate(),
            userProfile.getCin(),
            userProfile.getSex(),
            userProfile.getPhoneNumber()
        );
    }

    private boolean matchesQuery(SuperAdmin superAdmin, UserProfile userProfile, String query) {
        String fullName = (userProfile.getFirstName() + " " + userProfile.getLastName())
            .toLowerCase(Locale.ROOT);

        return fullName.contains(query)
            || superAdmin.getUserAccount().getUniversityEmail().toLowerCase(Locale.ROOT).contains(query)
            || userProfile.getCin() != null && userProfile.getCin().toLowerCase(Locale.ROOT).contains(query);
    }

    private void ensureAccountCanBeLocked(UserAccount userAccount) {
        if (userAccount.getAccountStatus() == AccountStatus.DEACTIVATED
            || userAccount.getAccountStatus() == AccountStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inactive account cannot be locked");
        }
    }

    private void ensureAccountCanBeUnlocked(UserAccount userAccount) {
        if (userAccount.getAccountStatus() != AccountStatus.LOCKED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only a locked account can be unlocked");
        }
    }

    private String normalizeEmail(String universityEmail) {
        return universityEmail.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeOptionalCin(String cin) {
        String normalized = normalizeOptional(cin);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
