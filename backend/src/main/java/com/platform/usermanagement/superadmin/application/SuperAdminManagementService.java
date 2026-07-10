package com.platform.usermanagement.superadmin.application;

import java.util.ArrayList;
import java.util.List;
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
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.usermanagement.superadmin.presentation.dto.CreateSuperAdminRequest;
import com.platform.usermanagement.superadmin.presentation.dto.CreateSuperAdminResponse;
import com.platform.usermanagement.superadmin.presentation.dto.ResetPasswordRequest;
import com.platform.usermanagement.superadmin.presentation.dto.SuperAdminProfileResponse;

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

        String universityEmail = request.universityEmail().trim();

        if (userAccountRepository.existsByUniversityEmail(universityEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "University email already exists");
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setUniversityEmail(universityEmail);
        userAccount.setRole(AccountRoleType.SUPER_ADMIN);
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
        userAccount.setAccountStatus(AccountStatus.DEACTIVATED);

        return new ActionResponse(true, "Account locked");
    }

    @Transactional
    public ActionResponse unlockAccount(UUID superAdminId) {
        SuperAdmin superAdmin = findSuperAdmin(superAdminId);
        UserAccount userAccount = findUserAccount(superAdmin);
        userAccount.setAccountStatus(AccountStatus.ACTIVE);

        return new ActionResponse(true, "Account unlocked");
    }


    @Transactional
    public SuperAdminProfileResponse getSuperAdmin(UUID superAdminId) {

        SuperAdmin superAdmin = findSuperAdmin(superAdminId);
    
        UserProfile userProfile = userProfileRepository.findByUserAccountId(superAdmin.getUserAccount().getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Super admin profile not found"));

        return new SuperAdminProfileResponse(
            superAdmin.getId(),
            superAdmin.getUserAccount().getId(),
            superAdmin.getEstablishment().getId(),
            superAdmin.getUserAccount().getUniversityEmail(),
            superAdmin.getUserAccount().getRole(),
            superAdmin.getUserAccount().getAccountStatus(),
            userProfile.getFirstName(),
            userProfile.getLastName(),
            userProfile.getSex(),
            userProfile.getPhoneNumber()

        );

    }


    @Transactional
    public List<SuperAdminProfileResponse> getSuperAdmins(UUID establishmentId) {

        if (!establishmentRepository.existsById(establishmentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Establishment not found");
        }

        List<SuperAdmin> superAdmins = superAdminRepository.findByEstablishmentId(establishmentId);

        List<SuperAdminProfileResponse> responses = new ArrayList<>();

        for (SuperAdmin superAdmin : superAdmins) {
            UserProfile userProfile = userProfileRepository.findByUserAccountId(superAdmin.getUserAccount().getId())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Super admin profile not found"
                ));

            responses.add(
                new SuperAdminProfileResponse(
                    superAdmin.getId(),
                    superAdmin.getUserAccount().getId(),
                    superAdmin.getEstablishment().getId(),
                    superAdmin.getUserAccount().getUniversityEmail(),
                    superAdmin.getUserAccount().getRole(),
                    superAdmin.getUserAccount().getAccountStatus(),
                    userProfile.getFirstName(),
                    userProfile.getLastName(),
                    userProfile.getSex(),
                    userProfile.getPhoneNumber()

                )
            );
        }

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
}
