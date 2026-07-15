package com.platform.universitygovernance.classgroup;

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
import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academiclevel.infrastructure.AcademicLevelRepository;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicyear.domain.AcademicYearStatus;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.classgroup.application.ClassGroupService;
import com.platform.universitygovernance.classgroup.domain.ClassGroupStatus;
import com.platform.universitygovernance.classgroup.infrastructure.ClassGroupRepository;
import com.platform.universitygovernance.classgroup.presentation.dto.ClassGroupResponse;
import com.platform.universitygovernance.classgroup.presentation.dto.CreateClassGroupRequest;
import com.platform.universitygovernance.classgroup.presentation.dto.UpdateClassGroupRequest;
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
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;
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
class ClassGroupServiceIntegrationTest {

    @Autowired
    private ClassGroupService classGroupService;

    @Autowired
    private ClassGroupRepository classGroupRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private AcademicLevelRepository academicLevelRepository;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private ProgramFiliereRepository programFiliereRepository;

    @Autowired
    private DegreeCycleRepository degreeCycleRepository;

    @Autowired
    private ProgramPathRepository programPathRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

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
    private AcademicLevel firstLevel;
    private AcademicLevel secondLevel;
    private AcademicYear firstYear;
    private AcademicYear nextFirstYear;
    private AcademicYear secondYear;

    @BeforeEach
    void setUp() {
        clearBusinessData();

        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);

        firstEstablishment = saveEstablishment(university, "ENSA Agadir", EstablishmentType.SCHOOL);
        secondEstablishment = saveEstablishment(university, "Faculty of Sciences", EstablishmentType.FACULTY);
        firstLevel = saveLevel(firstEstablishment, "Computer Science", "Master", "Excellence", "IL", "M1");
        secondLevel = saveLevel(secondEstablishment, "Mathematics", "Licence", "Regular", "MATH", "L1");
        firstYear = saveAcademicYear(firstEstablishment, "2026-2027", 2026);
        nextFirstYear = saveAcademicYear(firstEstablishment, "2027-2028", 2027);
        secondYear = saveAcademicYear(secondEstablishment, "2026-2027", 2026);
    }

    @AfterEach
    void tearDown() {
        clearBusinessData();
    }

    @Test
    void rootCanManageClassGroupsWithinLevelAndYear() {
        AuthenticatedUserPrincipal root = principal(AccountRoleType.ROOT_SUPER_ADMIN, UUID.randomUUID(), null);

        ClassGroupResponse groupB = classGroupService.createClassGroup(
            root,
            firstLevel.getId(),
            firstYear.getId(),
            new CreateClassGroupRequest(" Group B ", ClassGroupStatus.ACTIVE)
        );
        ClassGroupResponse groupA = classGroupService.createClassGroup(
            root,
            firstLevel.getId(),
            firstYear.getId(),
            new CreateClassGroupRequest("Group A", ClassGroupStatus.ACTIVE)
        );

        assertThat(groupB.name()).isEqualTo("Group B");
        assertThat(groupB.programFiliereId()).isEqualTo(firstLevel.getProgramFiliere().getId());
        assertThat(groupB.establishmentId()).isEqualTo(firstEstablishment.getId());
        assertThat(classGroupService.getClassGroups(root, firstLevel.getId(), firstYear.getId()))
            .extracting(ClassGroupResponse::id)
            .containsExactly(groupA.id(), groupB.id());
        assertThat(classGroupService.getClassGroup(root, groupA.id()).status())
            .isEqualTo(ClassGroupStatus.ACTIVE);

        assertThatThrownBy(() -> classGroupService.createClassGroup(
            root,
            firstLevel.getId(),
            firstYear.getId(),
            new CreateClassGroupRequest("group a", ClassGroupStatus.ACTIVE)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");

        ClassGroupResponse nextYearGroup = classGroupService.createClassGroup(
            root,
            firstLevel.getId(),
            nextFirstYear.getId(),
            new CreateClassGroupRequest("Group A", ClassGroupStatus.ACTIVE)
        );
        assertThat(nextYearGroup.academicYearId()).isEqualTo(nextFirstYear.getId());

        assertThatThrownBy(() -> classGroupService.createClassGroup(
            root,
            firstLevel.getId(),
            secondYear.getId(),
            new CreateClassGroupRequest("Group C", ClassGroupStatus.ACTIVE)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");

        ClassGroupResponse updated = classGroupService.updateClassGroup(
            root,
            groupB.id(),
            new UpdateClassGroupRequest("Group C", ClassGroupStatus.INACTIVE)
        );
        assertThat(updated.name()).isEqualTo("Group C");
        assertThat(updated.status()).isEqualTo(ClassGroupStatus.INACTIVE);
    }

    @Test
    void adminNeedsTheMatchingPermissionAndEstablishment() {
        AuthenticatedUserPrincipal root = principal(AccountRoleType.ROOT_SUPER_ADMIN, UUID.randomUUID(), null);
        ClassGroupResponse firstGroup = classGroupService.createClassGroup(
            root,
            firstLevel.getId(),
            firstYear.getId(),
            new CreateClassGroupRequest("Group A", ClassGroupStatus.ACTIVE)
        );
        ClassGroupResponse secondGroup = classGroupService.createClassGroup(
            root,
            secondLevel.getId(),
            secondYear.getId(),
            new CreateClassGroupRequest("Group A", ClassGroupStatus.ACTIVE)
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
            .findByCodeIn(Set.of(PermissionCode.CLASS_GROUP_UPDATE))
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

        assertThat(classGroupService.updateClassGroup(
            adminPrincipal,
            firstGroup.id(),
            new UpdateClassGroupRequest("Group 1", ClassGroupStatus.ARCHIVED)
        ).status()).isEqualTo(ClassGroupStatus.ARCHIVED);
        assertThatThrownBy(() -> classGroupService.getClassGroup(adminPrincipal, firstGroup.id()))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
        assertThatThrownBy(() -> classGroupService.updateClassGroup(
            adminPrincipal,
            secondGroup.id(),
            new UpdateClassGroupRequest("Denied", ClassGroupStatus.INACTIVE)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
    }

    private AcademicLevel saveLevel(
        Establishment establishment,
        String departmentName,
        String degreeCycleName,
        String programPathName,
        String programCode,
        String levelName
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

        ProgramFiliere program = new ProgramFiliere();
        program.setDepartment(department);
        program.setDegreeCycle(degreeCycle);
        program.setProgramPath(programPath);
        program.setCode(programCode);
        program.setName(departmentName + " Program");
        program = programFiliereRepository.save(program);

        AcademicLevel level = new AcademicLevel();
        level.setProgramFiliere(program);
        level.setName(levelName);
        level.setLevelOrder(1);
        return academicLevelRepository.save(level);
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
        classGroupRepository.deleteAll();
        semesterRepository.deleteAll();
        academicLevelRepository.deleteAll();
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
