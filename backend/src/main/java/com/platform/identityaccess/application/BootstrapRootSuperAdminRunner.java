package com.platform.identityaccess.application;

import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.UserAccount;
import com.platform.identityaccess.domain.UserProfile;
import com.platform.identityaccess.infrastructure.UserAccountRepository;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.identityaccess.domain.RootSuperAdmin;
import com.platform.identityaccess.infrastructure.RootSuperAdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
public class BootstrapRootSuperAdminRunner implements ApplicationRunner {

    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final RootSuperAdminRepository rootSuperAdminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${BOOTSTRAP_ROOT_EMAIL:}")
    private String bootstrapRootEmail;

    @Value("${BOOTSTRAP_ROOT_PASSWORD:}")
    private String bootstrapRootPassword;

    @Value("${BOOTSTRAP_ROOT_FIRST_NAME:}")
    private String bootstrapRootFirstName;

    @Value("${BOOTSTRAP_ROOT_LAST_NAME:}")
    private String bootstrapRootLastName;

    public BootstrapRootSuperAdminRunner(
        UserAccountRepository userAccountRepository,
        UserProfileRepository userProfileRepository,
        RootSuperAdminRepository rootSuperAdminRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.rootSuperAdminRepository = rootSuperAdminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (rootSuperAdminRepository.count() > 0) {
            return;
        }

        if (isBlank(bootstrapRootEmail)
            || isBlank(bootstrapRootPassword)
            || isBlank(bootstrapRootFirstName)
            || isBlank(bootstrapRootLastName)) {
            return;
        }

        if (userAccountRepository.existsByUniversityEmail(bootstrapRootEmail)) {
            return;
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setUniversityEmail(bootstrapRootEmail.trim());
        userAccount.setPasswordHash(passwordEncoder.encode(bootstrapRootPassword));
        userAccount.setRole(AccountRoleType.ROOT_SUPER_ADMIN);
        userAccount.setAccountStatus(AccountStatus.ACTIVE);
        userAccountRepository.save(userAccount);

        UserProfile userProfile = new UserProfile();
        userProfile.setUserAccount(userAccount);
        userProfile.setFirstName(bootstrapRootFirstName.trim());
        userProfile.setLastName(bootstrapRootLastName.trim());
        userProfileRepository.save(userProfile);

        RootSuperAdmin rootSuperAdmin = new RootSuperAdmin();
        rootSuperAdmin.setUserAccount(userAccount);
        rootSuperAdminRepository.save(rootSuperAdmin);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
