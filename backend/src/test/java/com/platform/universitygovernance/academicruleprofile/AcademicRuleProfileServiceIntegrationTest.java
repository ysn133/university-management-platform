package com.platform.universitygovernance.academicruleprofile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.Admin;
import com.platform.identityaccess.domain.AdminPermissionGrant;
import com.platform.identityaccess.domain.Permission;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.UserAccount;
import com.platform.identityaccess.infrastructure.AdminPermissionGrantRepository;
import com.platform.identityaccess.infrastructure.AdminRepository;
import com.platform.identityaccess.infrastructure.PermissionRepository;
import com.platform.identityaccess.infrastructure.RootSuperAdminRepository;
import com.platform.identityaccess.infrastructure.SuperAdminRepository;
import com.platform.identityaccess.infrastructure.UserAccountRepository;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.PlatformApplication;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.academicruleprofile.application.AcademicRuleProfileService;
import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfileStatus;
import com.platform.universitygovernance.academicruleprofile.domain.SessionGradePolicy;
import com.platform.universitygovernance.academicruleprofile.infrastructure.AcademicRuleProfileRepository;
import com.platform.universitygovernance.academicruleprofile.presentation.dto.AcademicRuleProfileResponse;
import com.platform.universitygovernance.academicruleprofile.presentation.dto.CreateAcademicRuleProfileRequest;
import com.platform.universitygovernance.academicruleprofile.presentation.dto.UpdateAcademicRuleProfileRequest;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.domain.EstablishmentStatus;
import com.platform.universitygovernance.establishment.domain.EstablishmentType;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.universitygovernance.university.domain.University;
import com.platform.universitygovernance.university.infrastructure.UniversityRepository;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(classes = PlatformApplication.class)
@ActiveProfiles("test")
class AcademicRuleProfileServiceIntegrationTest {

    @Autowired
    private AcademicRuleProfileService academicRuleProfileService;

    @Autowired
    private AcademicRuleProfileRepository academicRuleProfileRepository;

    @Autowired
    private AdminPermissionGrantRepository adminPermissionGrantRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private RootSuperAdminRepository rootSuperAdminRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private EstablishmentRepository establishmentRepository;

    @Autowired
    private UniversityRepository universityRepository;

    private Establishment firstEstablishment;
    private Establishment secondEstablishment;
    private AuthenticatedUserPrincipal root;

    @BeforeEach
    void setUp() {
        clearBusinessData();

        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);

        firstEstablishment = saveEstablishment(university, "ENSA Agadir", EstablishmentType.SCHOOL);
        secondEstablishment = saveEstablishment(
            university,
            "Faculty of Sciences",
            EstablishmentType.FACULTY
        );
        root = principal(AccountRoleType.ROOT_SUPER_ADMIN, UUID.randomUUID(), null);
    }

    @AfterEach
    void tearDown() {
        clearBusinessData();
    }

    @Test
    void rootCanCreateVersionListGetAndUpdateProfiles() {
        AcademicRuleProfileResponse firstVersion = academicRuleProfileService
            .createAcademicRuleProfile(
                root,
                firstEstablishment.getId(),
                createRequest("  Standard Master Rules  ")
            );
        AcademicRuleProfileResponse secondVersion = academicRuleProfileService
            .createAcademicRuleProfile(
                root,
                firstEstablishment.getId(),
                createRequest("standard master rules")
            );

        assertThat(firstVersion.name()).isEqualTo("Standard Master Rules");
        assertThat(firstVersion.version()).isEqualTo(1);
        assertThat(secondVersion.version()).isEqualTo(2);
        assertThat(academicRuleProfileService.getAcademicRuleProfiles(
            root,
            firstEstablishment.getId()
        ))
            .extracting(AcademicRuleProfileResponse::version)
            .containsExactly(2, 1);
        assertThat(academicRuleProfileService.getAcademicRuleProfile(root, firstVersion.id()))
            .extracting(
                AcademicRuleProfileResponse::moduleValidationThreshold,
                AcademicRuleProfileResponse::compensationMinimumThreshold,
                AcademicRuleProfileResponse::maximumCarriedModules
            )
            .containsExactly(
                new BigDecimal("10.00"),
                new BigDecimal("7.00"),
                2
            );

        AcademicRuleProfileResponse updated = academicRuleProfileService
            .updateAcademicRuleProfile(
                root,
                firstVersion.id(),
                new UpdateAcademicRuleProfileRequest(
                    "Standard Master Rules",
                    new BigDecimal("11.00"),
                    new BigDecimal("8.00"),
                    new BigDecimal("10.00"),
                    null,
                    2,
                    SessionGradePolicy.RATTRAPAGE_CAPPED_AT_VALIDATION_THRESHOLD,
                    false,
                    0,
                    AcademicRuleProfileStatus.INACTIVE
                )
            );

        assertThat(updated.version()).isEqualTo(1);
        assertThat(updated.moduleValidationThreshold()).isEqualByComparingTo("11.00");
        assertThat(updated.sessionGradePolicy())
            .isEqualTo(SessionGradePolicy.RATTRAPAGE_CAPPED_AT_VALIDATION_THRESHOLD);
        assertThat(updated.status()).isEqualTo(AcademicRuleProfileStatus.INACTIVE);
    }

    @Test
    void profileRulesMustBeInternallyConsistent() {
        CreateAcademicRuleProfileRequest invalidCompensation = new CreateAcademicRuleProfileRequest(
            "Invalid compensation",
            new BigDecimal("10.00"),
            new BigDecimal("11.00"),
            new BigDecimal("10.00"),
            null,
            2,
            SessionGradePolicy.BEST_GRADE,
            true,
            2,
            AcademicRuleProfileStatus.ACTIVE
        );
        assertThatThrownBy(() -> academicRuleProfileService.createAcademicRuleProfile(
            root,
            firstEstablishment.getId(),
            invalidCompensation
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST")
            .hasMessageContaining("cannot exceed");

        CreateAcademicRuleProfileRequest invalidProgression = new CreateAcademicRuleProfileRequest(
            "Invalid progression",
            new BigDecimal("10.00"),
            new BigDecimal("7.00"),
            new BigDecimal("10.00"),
            null,
            2,
            SessionGradePolicy.BEST_GRADE,
            false,
            2,
            AcademicRuleProfileStatus.ACTIVE
        );
        assertThatThrownBy(() -> academicRuleProfileService.createAcademicRuleProfile(
            root,
            firstEstablishment.getId(),
            invalidProgression
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST")
            .hasMessageContaining("must be zero");
    }

    @Test
    void adminNeedsSpecificPermissionsAndCannotCrossEstablishments() {
        AcademicRuleProfileResponse ownProfile = academicRuleProfileService
            .createAcademicRuleProfile(root, firstEstablishment.getId(), createRequest("Own Rules"));
        AcademicRuleProfileResponse otherProfile = academicRuleProfileService
            .createAcademicRuleProfile(root, secondEstablishment.getId(), createRequest("Other Rules"));

        UserAccount account = new UserAccount();
        account.setUniversityEmail("admin@ensa.uiz.ac.ma");
        account.setPasswordHash("not-used-by-this-test");
        account.setRole(AccountRoleType.ADMIN);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account = userAccountRepository.save(account);

        Admin admin = new Admin();
        admin.setUserAccount(account);
        admin.setEstablishment(firstEstablishment);
        admin = adminRepository.save(admin);

        Permission updatePermission = permissionRepository
            .findByCodeIn(Set.of(PermissionCode.ACADEMIC_RULE_PROFILE_UPDATE))
            .get(0);
        AdminPermissionGrant grant = new AdminPermissionGrant();
        grant.setAdmin(admin);
        grant.setPermission(updatePermission);
        adminPermissionGrantRepository.save(grant);

        AuthenticatedUserPrincipal adminPrincipal = principal(
            AccountRoleType.ADMIN,
            admin.getId(),
            firstEstablishment.getId()
        );

        assertThatThrownBy(() -> academicRuleProfileService.getAcademicRuleProfile(
            adminPrincipal,
            ownProfile.id()
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
        assertThat(academicRuleProfileService.updateAcademicRuleProfile(
            adminPrincipal,
            ownProfile.id(),
            updateRequest("Own Rules")
        ).maximumCarriedModules()).isEqualTo(3);
        assertThatThrownBy(() -> academicRuleProfileService.updateAcademicRuleProfile(
            adminPrincipal,
            otherProfile.id(),
            updateRequest("Other Rules")
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
        assertThatThrownBy(() -> academicRuleProfileService.createAcademicRuleProfile(
            adminPrincipal,
            firstEstablishment.getId(),
            createRequest("Denied Rules")
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
    }

    private CreateAcademicRuleProfileRequest createRequest(String name) {
        return new CreateAcademicRuleProfileRequest(
            name,
            new BigDecimal("10.00"),
            new BigDecimal("7.00"),
            new BigDecimal("10.00"),
            new BigDecimal("10.00"),
            2,
            SessionGradePolicy.BEST_GRADE,
            true,
            2,
            AcademicRuleProfileStatus.ACTIVE
        );
    }

    private UpdateAcademicRuleProfileRequest updateRequest(String name) {
        return new UpdateAcademicRuleProfileRequest(
            name,
            new BigDecimal("10.00"),
            new BigDecimal("7.00"),
            new BigDecimal("10.00"),
            new BigDecimal("10.00"),
            2,
            SessionGradePolicy.BEST_GRADE,
            true,
            3,
            AcademicRuleProfileStatus.ACTIVE
        );
    }

    private AuthenticatedUserPrincipal principal(
        AccountRoleType role,
        UUID roleEntityId,
        UUID establishmentId
    ) {
        return new AuthenticatedUserPrincipal(
            UUID.randomUUID(),
            role,
            roleEntityId,
            establishmentId,
            role.name().toLowerCase() + "@uiz.ac.ma"
        );
    }

    private Establishment saveEstablishment(
        University university,
        String name,
        EstablishmentType type
    ) {
        Establishment establishment = new Establishment();
        establishment.setUniversity(university);
        establishment.setName(name);
        establishment.setEstablishmentType(type);
        establishment.setEstablishmentStatus(EstablishmentStatus.ACTIVE);
        return establishmentRepository.save(establishment);
    }

    private void clearBusinessData() {
        adminPermissionGrantRepository.deleteAll();
        academicRuleProfileRepository.deleteAll();
        adminRepository.deleteAll();
        superAdminRepository.deleteAll();
        establishmentRepository.deleteAll();
        rootSuperAdminRepository.deleteAll();
        universityRepository.deleteAll();
        userProfileRepository.deleteAll();
        userAccountRepository.deleteAll();
    }
}
