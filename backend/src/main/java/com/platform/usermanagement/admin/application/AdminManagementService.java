package com.platform.usermanagement.admin.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.Admin;
import com.platform.identityaccess.domain.UserAccount;
import com.platform.identityaccess.domain.UserProfile;
import com.platform.identityaccess.infrastructure.AdminRepository;
import com.platform.identityaccess.infrastructure.UserAccountRepository;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.usermanagement.admin.presentation.dto.AdminProfileResponse;
import com.platform.usermanagement.admin.presentation.dto.CreateAdminRequest;
import com.platform.usermanagement.admin.presentation.dto.CreateAdminResponse;
import com.platform.usermanagement.admin.presentation.dto.ResetAdminPasswordRequest;

import jakarta.transaction.Transactional;

@Service
public class AdminManagementService {

    private final EstablishmentRepository establishmentRepository;
    private final AdminRepository adminRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminManagementService(
        EstablishmentRepository establishmentRepository,
        AdminRepository adminRepository,
        UserAccountRepository userAccountRepository,
        UserProfileRepository userProfileRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.establishmentRepository = establishmentRepository;
        this.adminRepository = adminRepository;
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CreateAdminResponse createAdmin(
        AuthenticatedUserPrincipal principal,
        CreateAdminRequest request,
        UUID establishmentId
    ) {
        ensureCallerCanCreateAdmin(principal, establishmentId);

        Establishment establishment = establishmentRepository.findById(establishmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Establishment not found"));

        String universityEmail = request.universityEmail().trim();
        if (userAccountRepository.existsByUniversityEmail(universityEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "University email already exists");
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setUniversityEmail(universityEmail);
        userAccount.setRole(AccountRoleType.ADMIN);
        userAccount.setPasswordHash(passwordEncoder.encode(request.password()));
        userAccount.setAccountStatus(AccountStatus.ACTIVE);

        UserAccount createdUserAccount = userAccountRepository.save(userAccount);

        UserProfile userProfile = new UserProfile();
        userProfile.setUserAccount(createdUserAccount);
        userProfile.setFirstName(request.firstName());
        userProfile.setLastName(request.lastName());
        userProfile.setBirthDate(request.birthDate());
        userProfile.setSex(request.sex());
        userProfile.setPhoneNumber(request.phoneNumber());
        userProfileRepository.save(userProfile);

        Admin admin = new Admin();
        admin.setUserAccount(createdUserAccount);
        admin.setEstablishment(establishment);
        Admin createdAdmin = adminRepository.save(admin);

        return new CreateAdminResponse(
            createdAdmin.getId(),
            createdUserAccount.getId(),
            establishmentId,
            createdUserAccount.getRole()
        );
    }

    @Transactional
    public AdminProfileResponse getAdmin(AuthenticatedUserPrincipal principal, UUID adminId) {
        Admin admin = findAdmin(adminId);
        ensureCallerCanManageEstablishment(principal, admin.getEstablishment().getId());

        UserProfile profile = findProfile(admin);

        return new AdminProfileResponse(
            admin.getId(),
            admin.getUserAccount().getId(),
            admin.getEstablishment().getId(),
            admin.getUserAccount().getUniversityEmail(),
            admin.getUserAccount().getRole(),
            admin.getUserAccount().getAccountStatus(),
            profile.getFirstName(),
            profile.getLastName(),
            profile.getSex(),
            profile.getPhoneNumber()
        );
    }

    @Transactional
    public List<AdminProfileResponse> getAdmins(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        ensureCallerCanManageEstablishment(principal, establishmentId);

        if (!establishmentRepository.existsById(establishmentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Establishment not found");
        }

        List<AdminProfileResponse> responses = new ArrayList<>();

        for (Admin admin : adminRepository.findByEstablishmentId(establishmentId)) {
            UserProfile profile = findProfile(admin);

            responses.add(new AdminProfileResponse(
                admin.getId(),
                admin.getUserAccount().getId(),
                admin.getEstablishment().getId(),
                admin.getUserAccount().getUniversityEmail(),
                admin.getUserAccount().getRole(),
                admin.getUserAccount().getAccountStatus(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getSex(),
                profile.getPhoneNumber()
            ));
        }

        return responses;
    }

    @Transactional
    public ActionResponse resetPassword(
        AuthenticatedUserPrincipal principal,
        UUID adminId,
        ResetAdminPasswordRequest request
    ) {
        Admin admin = findManagedAdmin(principal, adminId);
        UserAccount account = admin.getUserAccount();

        if (passwordEncoder.matches(request.newPassword(), account.getPasswordHash())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "New password must be different from the current password"
            );
        }

        account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        return new ActionResponse(true, "Password has been reset");
    }

    @Transactional
    public ActionResponse lockAccount(AuthenticatedUserPrincipal principal, UUID adminId) {
        Admin admin = findManagedAdmin(principal, adminId);
        UserAccount account = admin.getUserAccount();

        ensureAccountCanBeLocked(account);
        account.setAccountStatus(AccountStatus.LOCKED);
        return new ActionResponse(true, "Account locked");
    }

    @Transactional
    public ActionResponse unlockAccount(AuthenticatedUserPrincipal principal, UUID adminId) {
        Admin admin = findManagedAdmin(principal, adminId);
        UserAccount account = admin.getUserAccount();

        ensureAccountCanBeUnlocked(account);
        account.setAccountStatus(AccountStatus.ACTIVE);
        return new ActionResponse(true, "Account unlocked");
    }

    @Transactional
    public ActionResponse deactivateAccount(AuthenticatedUserPrincipal principal, UUID adminId) {
        Admin admin = findManagedAdmin(principal, adminId);
        UserAccount account = admin.getUserAccount();

        if (account.getAccountStatus() == AccountStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Archived account cannot be deactivated");
        }

        account.setAccountStatus(AccountStatus.DEACTIVATED);
        return new ActionResponse(true, "Account deactivated");
    }

    @Transactional
    public ActionResponse archiveAccount(AuthenticatedUserPrincipal principal, UUID adminId) {
        Admin admin = findManagedAdmin(principal, adminId);
        admin.getUserAccount().setAccountStatus(AccountStatus.ARCHIVED);
        return new ActionResponse(true, "Account archived");
    }

    private void ensureCallerCanCreateAdmin(AuthenticatedUserPrincipal principal, UUID establishmentId) {
        ensureCallerCanManageEstablishment(principal, establishmentId);
    }

    private void ensureCallerCanManageEstablishment(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        if (principal.role() == AccountRoleType.ROOT_SUPER_ADMIN) {
            return;
        }

        if (principal.role() == AccountRoleType.SUPER_ADMIN && establishmentId.equals(principal.establishmentId())) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot manage admins for this establishment");
    }

    private Admin findAdmin(UUID adminId) {
        return adminRepository.findById(adminId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
    }

    private Admin findManagedAdmin(AuthenticatedUserPrincipal principal, UUID adminId) {
        Admin admin = findAdmin(adminId);
        ensureCallerCanManageEstablishment(principal, admin.getEstablishment().getId());
        return admin;
    }

    private UserProfile findProfile(Admin admin) {
        return userProfileRepository.findByUserAccountId(admin.getUserAccount().getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin profile not found"));
    }

    private void ensureAccountCanBeLocked(UserAccount account) {
        if (account.getAccountStatus() == AccountStatus.DEACTIVATED
            || account.getAccountStatus() == AccountStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inactive account cannot be locked");
        }
    }

    private void ensureAccountCanBeUnlocked(UserAccount account) {
        if (account.getAccountStatus() == AccountStatus.DEACTIVATED
            || account.getAccountStatus() == AccountStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inactive account cannot be unlocked");
        }
    }
}
