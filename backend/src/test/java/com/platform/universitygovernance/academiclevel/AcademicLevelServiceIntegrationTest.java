package com.platform.universitygovernance.academiclevel;

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
import com.platform.universitygovernance.academiclevel.application.AcademicLevelService;
import com.platform.universitygovernance.academiclevel.infrastructure.AcademicLevelRepository;
import com.platform.universitygovernance.academiclevel.presentation.dto.AcademicLevelResponse;
import com.platform.universitygovernance.academiclevel.presentation.dto.CreateAcademicLevelRequest;
import com.platform.universitygovernance.academiclevel.presentation.dto.UpdateAcademicLevelRequest;
import com.platform.universitygovernance.academiclevelruleassignment.infrastructure.AcademicLevelRuleAssignmentRepository;
import com.platform.universitygovernance.academiclevelruleassignment.application.AcademicLevelRuleAssignmentService;
import com.platform.universitygovernance.academiclevelruleassignment.presentation.dto.AcademicLevelRuleAssignmentResponse;
import com.platform.universitygovernance.academiclevelruleassignment.presentation.dto.CreateAcademicLevelRuleAssignmentRequest;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicyear.domain.AcademicYearStatus;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfile;
import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfileStatus;
import com.platform.universitygovernance.academicruleprofile.domain.SessionGradePolicy;
import com.platform.universitygovernance.academicruleprofile.infrastructure.AcademicRuleProfileRepository;
import com.platform.universitygovernance.academicruleprofile.application.AcademicRuleProfileService;
import com.platform.universitygovernance.academicruleprofile.presentation.dto.UpdateAcademicRuleProfileRequest;
import com.platform.universitygovernance.degreecycle.domain.DegreeCycle;
import com.platform.universitygovernance.degreecycle.infrastructure.DegreeCycleRepository;
import com.platform.universitygovernance.department.domain.Department;
import com.platform.universitygovernance.department.infrastructure.DepartmentRepository;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.domain.EstablishmentStatus;
import com.platform.universitygovernance.establishment.domain.EstablishmentType;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.universitygovernance.programfiliere.domain.ProgramFiliere;
import com.platform.universitygovernance.programfiliere.infrastructure.ProgramFiliereRepository;
import com.platform.universitygovernance.programpath.domain.ProgramPath;
import com.platform.universitygovernance.programpath.infrastructure.ProgramPathRepository;
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
class AcademicLevelServiceIntegrationTest {

    @Autowired
    private AcademicLevelService academicLevelService;

    @Autowired
    private AcademicLevelRepository academicLevelRepository;

    @Autowired
    private AcademicLevelRuleAssignmentRepository ruleAssignmentRepository;

    @Autowired
    private AcademicLevelRuleAssignmentService ruleAssignmentService;

    @Autowired
    private AcademicRuleProfileRepository academicRuleProfileRepository;

    @Autowired
    private AcademicRuleProfileService academicRuleProfileService;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private ProgramFiliereRepository programFiliereRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DegreeCycleRepository degreeCycleRepository;

    @Autowired
    private ProgramPathRepository programPathRepository;

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
    private ProgramFiliere firstProgram;
    private ProgramFiliere secondProgram;
    private AcademicYear firstYear;
    private AcademicYear secondYear;
    private AcademicRuleProfile firstRuleProfile;
    private AcademicRuleProfile secondRuleProfile;

    @BeforeEach
    void setUp() {
        clearBusinessData();

        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);

        firstEstablishment = saveEstablishment(university, "ENSA Agadir", EstablishmentType.SCHOOL);
        secondEstablishment = saveEstablishment(university, "Faculty of Sciences", EstablishmentType.FACULTY);
        firstProgram = saveProgram(firstEstablishment, "Computer Science", "Master", "Excellence", "IL");
        secondProgram = saveProgram(secondEstablishment, "Mathematics", "Licence", "Regular", "MATH");
        firstYear = saveAcademicYear(firstEstablishment, "2026-2027", 2026);
        secondYear = saveAcademicYear(secondEstablishment, "2026-2027", 2026);
        firstRuleProfile = saveRuleProfile(firstEstablishment, "Master Rules");
        secondRuleProfile = saveRuleProfile(secondEstablishment, "Licence Rules");
    }

    @AfterEach
    void tearDown() {
        clearBusinessData();
    }

    @Test
    void rootCanManageAcademicLevelsAndUniquenessIsEnforcedPerProgram() {
        AuthenticatedUserPrincipal root = principal(AccountRoleType.ROOT_SUPER_ADMIN, UUID.randomUUID(), null);

        assertThatThrownBy(() -> academicLevelService.createAcademicLevel(
            root,
            firstProgram.getId(),
            new CreateAcademicLevelRequest(
                "M2",
                2,
                true,
                firstYear.getId(),
                firstRuleProfile.getId()
            )
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("final academic level")
            .hasMessageContaining("progression with debt");

        AcademicRuleProfile terminalRuleProfile = saveRuleProfile(
            firstEstablishment,
            "Terminal Master Rules"
        );
        terminalRuleProfile.setAllowProgressionWithDebt(false);
        terminalRuleProfile.setMaximumCarriedModules(0);
        terminalRuleProfile = academicRuleProfileRepository.save(terminalRuleProfile);

        AcademicLevelResponse second = academicLevelService.createAcademicLevel(
            root,
            firstProgram.getId(),
            new CreateAcademicLevelRequest(
                "  M2  ",
                2,
                true,
                firstYear.getId(),
                terminalRuleProfile.getId()
            )
        );
        AcademicLevelResponse first = academicLevelService.createAcademicLevel(
            root,
            firstProgram.getId(),
            new CreateAcademicLevelRequest(
                "M1",
                1,
                false,
                firstYear.getId(),
                firstRuleProfile.getId()
            )
        );

        assertThat(second.name()).isEqualTo("M2");
        assertThat(second.terminalLevel()).isTrue();
        assertThat(second.establishmentId()).isEqualTo(firstEstablishment.getId());
        assertThat(ruleAssignmentRepository.findByAcademicLevelIdAndAcademicYearId(
            second.id(),
            firstYear.getId()
        )).isPresent();
        assertThat(academicLevelService.getAcademicLevels(root, firstProgram.getId()))
            .extracting(AcademicLevelResponse::id)
            .containsExactly(first.id(), second.id());
        assertThat(academicLevelService.getAcademicLevel(root, first.id()).id()).isEqualTo(first.id());

        AcademicYear nextYear = saveAcademicYear(firstEstablishment, "2027-2028", 2027);
        AcademicLevelRuleAssignmentResponse nextAssignment = ruleAssignmentService.createAssignment(
            root,
            first.id(),
            new CreateAcademicLevelRuleAssignmentRequest(
                nextYear.getId(),
                firstRuleProfile.getId()
            )
        );
        assertThat(ruleAssignmentService.getAssignments(root, first.id()))
            .extracting(AcademicLevelRuleAssignmentResponse::academicYearId)
            .containsExactly(nextYear.getId(), firstYear.getId());
        assertThat(ruleAssignmentService.getAssignment(root, nextAssignment.id()).id())
            .isEqualTo(nextAssignment.id());
        assertThatThrownBy(() -> ruleAssignmentService.createAssignment(
            root,
            first.id(),
            new CreateAcademicLevelRuleAssignmentRequest(
                nextYear.getId(),
                firstRuleProfile.getId()
            )
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");
        assertThatThrownBy(() -> ruleAssignmentService.createAssignment(
            root,
            first.id(),
            new CreateAcademicLevelRuleAssignmentRequest(
                firstYear.getId(),
                secondRuleProfile.getId()
            )
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");
        assertThatThrownBy(() -> academicRuleProfileService.updateAcademicRuleProfile(
            root,
            firstRuleProfile.getId(),
            new UpdateAcademicRuleProfileRequest(
                "Master Rules",
                new BigDecimal("11.00"),
                new BigDecimal("7.00"),
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                2,
                SessionGradePolicy.BEST_GRADE,
                true,
                2,
                AcademicRuleProfileStatus.ACTIVE
            )
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT")
            .hasMessageContaining("immutable");
        assertThat(academicRuleProfileService.updateAcademicRuleProfile(
            root,
            firstRuleProfile.getId(),
            new UpdateAcademicRuleProfileRequest(
                "Master Rules",
                new BigDecimal("10.00"),
                new BigDecimal("7.00"),
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                2,
                SessionGradePolicy.BEST_GRADE,
                true,
                2,
                AcademicRuleProfileStatus.INACTIVE
            )
        ).status()).isEqualTo(AcademicRuleProfileStatus.INACTIVE);

        assertThatThrownBy(() -> academicLevelService.createAcademicLevel(
            root,
            firstProgram.getId(),
            new CreateAcademicLevelRequest(
                "m1",
                3,
                false,
                firstYear.getId(),
                firstRuleProfile.getId()
            )
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");
        assertThatThrownBy(() -> academicLevelService.createAcademicLevel(
            root,
            firstProgram.getId(),
            new CreateAcademicLevelRequest(
                "M3",
                2,
                false,
                firstYear.getId(),
                firstRuleProfile.getId()
            )
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");

        AcademicLevelResponse updated = academicLevelService.updateAcademicLevel(
            root,
            second.id(),
            new UpdateAcademicLevelRequest("Final year", 3, true)
        );
        assertThat(updated.name()).isEqualTo("Final year");
        assertThat(updated.levelOrder()).isEqualTo(3);
        assertThat(updated.terminalLevel()).isTrue();

        assertThat(academicLevelService.deleteAcademicLevel(root, updated.id()).success()).isTrue();
        assertThat(academicLevelRepository.findById(updated.id())).isEmpty();
    }

    @Test
    void adminNeedsTheMatchingPermissionAndEstablishment() {
        AuthenticatedUserPrincipal root = principal(AccountRoleType.ROOT_SUPER_ADMIN, UUID.randomUUID(), null);
        AcademicLevelResponse firstLevel = academicLevelService.createAcademicLevel(
            root,
            firstProgram.getId(),
            new CreateAcademicLevelRequest(
                "M1",
                1,
                false,
                firstYear.getId(),
                firstRuleProfile.getId()
            )
        );
        AcademicLevelResponse secondLevel = academicLevelService.createAcademicLevel(
            root,
            secondProgram.getId(),
            new CreateAcademicLevelRequest(
                "L1",
                1,
                false,
                secondYear.getId(),
                secondRuleProfile.getId()
            )
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
            .findByCodeIn(Set.of(PermissionCode.ACADEMIC_LEVEL_UPDATE))
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

        assertThat(academicLevelService.updateAcademicLevel(
            adminPrincipal,
            firstLevel.id(),
            new UpdateAcademicLevelRequest("Master 1", 1, false)
        ).name()).isEqualTo("Master 1");
        assertThatThrownBy(() -> academicLevelService.getAcademicLevel(adminPrincipal, firstLevel.id()))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
        assertThatThrownBy(() -> academicLevelService.updateAcademicLevel(
            adminPrincipal,
            secondLevel.id(),
            new UpdateAcademicLevelRequest("Denied", 2, false)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
    }

    private ProgramFiliere saveProgram(
        Establishment establishment,
        String departmentName,
        String degreeCycleName,
        String programPathName,
        String code
    ) {
        Department department = new Department();
        department.setEstablishment(establishment);
        department.setName(departmentName);
        department = departmentRepository.save(department);

        DegreeCycle degreeCycle = new DegreeCycle();
        degreeCycle.setEstablishment(establishment);
        degreeCycle.setName(degreeCycleName);
        degreeCycle = degreeCycleRepository.save(degreeCycle);

        ProgramPath programPath = new ProgramPath();
        programPath.setEstablishment(establishment);
        programPath.setName(programPathName);
        programPath = programPathRepository.save(programPath);

        ProgramFiliere programFiliere = new ProgramFiliere();
        programFiliere.setDepartment(department);
        programFiliere.setDegreeCycle(degreeCycle);
        programFiliere.setProgramPath(programPath);
        programFiliere.setCode(code);
        programFiliere.setName(departmentName + " Program");
        return programFiliereRepository.save(programFiliere);
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

    private AcademicYear saveAcademicYear(
        Establishment establishment,
        String label,
        int startYear
    ) {
        AcademicYear academicYear = new AcademicYear();
        academicYear.setEstablishment(establishment);
        academicYear.setLabel(label);
        academicYear.setStartYear(startYear);
        academicYear.setEndYear(startYear + 1);
        academicYear.setStatus(AcademicYearStatus.ACTIVE);
        return academicYearRepository.save(academicYear);
    }

    private AcademicRuleProfile saveRuleProfile(Establishment establishment, String name) {
        AcademicRuleProfile profile = new AcademicRuleProfile();
        profile.setEstablishment(establishment);
        profile.setName(name);
        profile.setVersion(1);
        profile.setModuleValidationThreshold(new BigDecimal("10.00"));
        profile.setCompensationMinimumThreshold(new BigDecimal("7.00"));
        profile.setSemesterValidationAverage(new BigDecimal("10.00"));
        profile.setAnnualValidationAverage(new BigDecimal("10.00"));
        profile.setMaximumModuleInscriptions(2);
        profile.setSessionGradePolicy(SessionGradePolicy.BEST_GRADE);
        profile.setAllowProgressionWithDebt(true);
        profile.setMaximumCarriedModules(2);
        profile.setStatus(AcademicRuleProfileStatus.ACTIVE);
        return academicRuleProfileRepository.save(profile);
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
        ruleAssignmentRepository.deleteAll();
        academicLevelRepository.deleteAll();
        academicYearRepository.deleteAll();
        academicRuleProfileRepository.deleteAll();
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
