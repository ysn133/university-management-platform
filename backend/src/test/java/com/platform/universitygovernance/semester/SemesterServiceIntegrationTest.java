package com.platform.universitygovernance.semester;

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
import com.platform.universitygovernance.semester.application.SemesterService;
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;
import com.platform.universitygovernance.semester.presentation.dto.CreateSemesterRequest;
import com.platform.universitygovernance.semester.presentation.dto.SemesterResponse;
import com.platform.universitygovernance.semester.presentation.dto.UpdateSemesterRequest;
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
class SemesterServiceIntegrationTest {

    @Autowired
    private SemesterService semesterService;

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
    void rootCanManageYearSpecificSemesters() {
        AuthenticatedUserPrincipal root = principal(AccountRoleType.ROOT_SUPER_ADMIN, UUID.randomUUID(), null);

        SemesterResponse second = semesterService.createSemester(
            root,
            firstLevel.getId(),
            firstYear.getId(),
            new CreateSemesterRequest(" s2 ", 2)
        );
        SemesterResponse first = semesterService.createSemester(
            root,
            firstLevel.getId(),
            firstYear.getId(),
            new CreateSemesterRequest("S1", 1)
        );

        assertThat(second.name()).isEqualTo("S2");
        assertThat(second.establishmentId()).isEqualTo(firstEstablishment.getId());
        assertThat(semesterService.getSemesters(root, firstLevel.getId(), firstYear.getId()))
            .extracting(SemesterResponse::id)
            .containsExactly(first.id(), second.id());
        assertThat(semesterService.getSemester(root, first.id()).academicYearId())
            .isEqualTo(firstYear.getId());

        assertThatThrownBy(() -> semesterService.createSemester(
            root,
            firstLevel.getId(),
            firstYear.getId(),
            new CreateSemesterRequest("s1", 3)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");
        assertThatThrownBy(() -> semesterService.createSemester(
            root,
            firstLevel.getId(),
            firstYear.getId(),
            new CreateSemesterRequest("S3", 2)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");

        SemesterResponse nextYearSemester = semesterService.createSemester(
            root,
            firstLevel.getId(),
            nextFirstYear.getId(),
            new CreateSemesterRequest("S1", 1)
        );
        assertThat(nextYearSemester.academicYearId()).isEqualTo(nextFirstYear.getId());

        assertThatThrownBy(() -> semesterService.createSemester(
            root,
            firstLevel.getId(),
            secondYear.getId(),
            new CreateSemesterRequest("S1", 1)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");

        SemesterResponse updated = semesterService.updateSemester(
            root,
            second.id(),
            new UpdateSemesterRequest("S3", 3)
        );
        assertThat(updated.name()).isEqualTo("S3");
        assertThat(updated.semesterOrder()).isEqualTo(3);

        assertThat(semesterService.deleteSemester(root, updated.id()).success()).isTrue();
        assertThat(semesterRepository.findById(updated.id())).isEmpty();
    }

    @Test
    void adminNeedsTheMatchingPermissionAndEstablishment() {
        AuthenticatedUserPrincipal root = principal(AccountRoleType.ROOT_SUPER_ADMIN, UUID.randomUUID(), null);
        SemesterResponse firstSemester = semesterService.createSemester(
            root,
            firstLevel.getId(),
            firstYear.getId(),
            new CreateSemesterRequest("S1", 1)
        );
        SemesterResponse secondSemester = semesterService.createSemester(
            root,
            secondLevel.getId(),
            secondYear.getId(),
            new CreateSemesterRequest("S1", 1)
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
            .findByCodeIn(Set.of(PermissionCode.SEMESTER_UPDATE))
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

        assertThat(semesterService.updateSemester(
            adminPrincipal,
            firstSemester.id(),
            new UpdateSemesterRequest("Semester 1", 1)
        ).name()).isEqualTo("SEMESTER 1");
        assertThatThrownBy(() -> semesterService.getSemester(adminPrincipal, firstSemester.id()))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
        assertThatThrownBy(() -> semesterService.updateSemester(
            adminPrincipal,
            secondSemester.id(),
            new UpdateSemesterRequest("Denied", 2)
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
