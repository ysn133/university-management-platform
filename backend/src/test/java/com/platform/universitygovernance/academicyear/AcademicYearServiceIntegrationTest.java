package com.platform.universitygovernance.academicyear;

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
import com.platform.universitygovernance.academicyear.application.AcademicYearService;
import com.platform.universitygovernance.academicyear.domain.AcademicYearStatus;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.academicyear.presentation.dto.AcademicYearResponse;
import com.platform.universitygovernance.academicyear.presentation.dto.CreateAcademicYearRequest;
import com.platform.universitygovernance.academicyear.presentation.dto.UpdateAcademicYearRequest;
import com.platform.universitygovernance.degreecycle.infrastructure.DegreeCycleRepository;
import com.platform.universitygovernance.department.infrastructure.DepartmentRepository;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.domain.EstablishmentStatus;
import com.platform.universitygovernance.establishment.domain.EstablishmentType;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.universitygovernance.programfiliere.infrastructure.ProgramFiliereRepository;
import com.platform.universitygovernance.programpath.infrastructure.ProgramPathRepository;
import com.platform.universitygovernance.university.domain.University;
import com.platform.universitygovernance.university.infrastructure.UniversityRepository;
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
class AcademicYearServiceIntegrationTest {

    @Autowired
    private AcademicYearService academicYearService;

    @Autowired
    private AcademicYearRepository academicYearRepository;

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
    private ProgramFiliereRepository programFiliereRepository;

    @Autowired
    private DegreeCycleRepository degreeCycleRepository;

    @Autowired
    private ProgramPathRepository programPathRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EstablishmentRepository establishmentRepository;

    @Autowired
    private UniversityRepository universityRepository;

    private Establishment firstEstablishment;
    private Establishment secondEstablishment;

    @BeforeEach
    void setUp() {
        clearBusinessData();

        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);

        firstEstablishment = saveEstablishment(university, "ENSA Agadir", EstablishmentType.SCHOOL);
        secondEstablishment = saveEstablishment(university, "Faculty of Sciences", EstablishmentType.FACULTY);
    }

    @AfterEach
    void tearDown() {
        clearBusinessData();
    }

    @Test
    void rootCanManageAcademicYearsAndDuplicateLabelsAreRejected() {
        AuthenticatedUserPrincipal root = principal(AccountRoleType.ROOT_SUPER_ADMIN, UUID.randomUUID(), null);

        AcademicYearResponse older = academicYearService.createAcademicYear(
            root,
            firstEstablishment.getId(),
            new CreateAcademicYearRequest("2025-2026", AcademicYearStatus.CLOSED)
        );
        AcademicYearResponse current = academicYearService.createAcademicYear(
            root,
            firstEstablishment.getId(),
            new CreateAcademicYearRequest("2026-2027", AcademicYearStatus.ACTIVE)
        );

        assertThat(academicYearService.getAcademicYears(root, firstEstablishment.getId()))
            .extracting(AcademicYearResponse::id)
            .containsExactly(current.id(), older.id());
        assertThat(academicYearService.getAcademicYear(root, current.id()).status())
            .isEqualTo(AcademicYearStatus.ACTIVE);
        assertThat(current.startYear()).isEqualTo(2026);
        assertThat(current.endYear()).isEqualTo(2027);

        assertThatThrownBy(() -> academicYearService.createAcademicYear(
            root,
            firstEstablishment.getId(),
            new CreateAcademicYearRequest("2026-2027", AcademicYearStatus.PLANNED)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");
        assertThatThrownBy(() -> academicYearService.createAcademicYear(
            root,
            firstEstablishment.getId(),
            new CreateAcademicYearRequest("2026-2028", AcademicYearStatus.PLANNED)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");

        AcademicYearResponse updated = academicYearService.updateAcademicYear(
            root,
            current.id(),
            new UpdateAcademicYearRequest("2027-2028", AcademicYearStatus.PLANNED)
        );
        assertThat(updated.label()).isEqualTo("2027-2028");
        assertThat(updated.startYear()).isEqualTo(2027);
        assertThat(updated.endYear()).isEqualTo(2028);
        assertThat(updated.status()).isEqualTo(AcademicYearStatus.PLANNED);

        assertThat(academicYearService.deleteAcademicYear(root, updated.id()).success()).isTrue();
        assertThat(academicYearRepository.findById(updated.id())).isEmpty();
    }

    @Test
    void adminNeedsTheMatchingPermissionAndEstablishment() {
        AuthenticatedUserPrincipal root = principal(AccountRoleType.ROOT_SUPER_ADMIN, UUID.randomUUID(), null);
        AcademicYearResponse firstYear = academicYearService.createAcademicYear(
            root,
            firstEstablishment.getId(),
            new CreateAcademicYearRequest("2026-2027", AcademicYearStatus.ACTIVE)
        );
        AcademicYearResponse secondYear = academicYearService.createAcademicYear(
            root,
            secondEstablishment.getId(),
            new CreateAcademicYearRequest("2026-2027", AcademicYearStatus.ACTIVE)
        );

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
            .findByCodeIn(Set.of(PermissionCode.ACADEMIC_YEAR_UPDATE))
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

        assertThat(academicYearService.updateAcademicYear(
            adminPrincipal,
            firstYear.id(),
            new UpdateAcademicYearRequest("2027-2028", AcademicYearStatus.PLANNED)
        ).label()).isEqualTo("2027-2028");
        assertThatThrownBy(() -> academicYearService.getAcademicYear(adminPrincipal, firstYear.id()))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
        assertThatThrownBy(() -> academicYearService.updateAcademicYear(
            adminPrincipal,
            secondYear.id(),
            new UpdateAcademicYearRequest("2027-2028", AcademicYearStatus.PLANNED)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
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
        academicYearRepository.deleteAll();
        programFiliereRepository.deleteAll();
        degreeCycleRepository.deleteAll();
        programPathRepository.deleteAll();
        departmentRepository.deleteAll();
        adminRepository.deleteAll();
        superAdminRepository.deleteAll();
        establishmentRepository.deleteAll();
        rootSuperAdminRepository.deleteAll();
        universityRepository.deleteAll();
        userProfileRepository.deleteAll();
        userAccountRepository.deleteAll();
    }
}
