package com.platform.universitygovernance.subjectmodules;

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
import com.platform.universitygovernance.academicdomain.domain.AcademicDomain;
import com.platform.universitygovernance.academicdomain.infrastructure.AcademicDomainRepository;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicyear.domain.AcademicYearStatus;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.classgroup.infrastructure.ClassGroupRepository;
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
import com.platform.universitygovernance.semester.domain.Semester;
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;
import com.platform.universitygovernance.subjectmodules.application.SubjectModuleService;
import com.platform.universitygovernance.subjectmodules.infrastructure.SubjectModuleRepository;
import com.platform.universitygovernance.subjectmodules.infrastructure.SubjectModuleDomainRepository;
import com.platform.universitygovernance.subjectmodules.presentation.dto.CreateSubjectModuleRequest;
import com.platform.universitygovernance.subjectmodules.presentation.dto.SubjectModuleResponse;
import com.platform.universitygovernance.subjectmodules.presentation.dto.UpdateSubjectModuleRequest;
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
class SubjectModuleServiceIntegrationTest {

    @Autowired
    private SubjectModuleService subjectModuleService;

    @Autowired
    private SubjectModuleRepository subjectModuleRepository;

    @Autowired
    private SubjectModuleDomainRepository subjectModuleDomainRepository;

    @Autowired
    private AcademicDomainRepository academicDomainRepository;

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
    private Semester firstSemester;
    private Semester secondSemester;

    @BeforeEach
    void setUp() {
        clearBusinessData();

        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);

        firstEstablishment = saveEstablishment(university, "ENSA Agadir", EstablishmentType.SCHOOL);
        secondEstablishment = saveEstablishment(university, "Faculty of Sciences", EstablishmentType.FACULTY);
        firstSemester = saveSemester(firstEstablishment, "Computer Science", "Master", "Excellence", "IL", "M1");
        secondSemester = saveSemester(secondEstablishment, "Mathematics", "Licence", "Regular", "MATH", "L1");
    }

    @AfterEach
    void tearDown() {
        clearBusinessData();
    }

    @Test
    void rootCanManageSubjectModulesAndCodesAreUniquePerSemester() {
        AuthenticatedUserPrincipal root = principal(AccountRoleType.ROOT_SUPER_ADMIN, UUID.randomUUID(), null);
        AcademicDomain databasesDomain = saveAcademicDomain(
            firstEstablishment,
            "DB",
            "Databases"
        );
        AcademicDomain softwareDomain = saveAcademicDomain(
            firstEstablishment,
            "SE",
            "Software Engineering"
        );
        AcademicDomain mathematicsDomain = saveAcademicDomain(
            secondEstablishment,
            "MATH",
            "Mathematics"
        );

        SubjectModuleResponse databases = subjectModuleService.createSubjectModule(
            root,
            firstSemester.getId(),
            new CreateSubjectModuleRequest(
                " DB101 ",
                "Databases",
                Set.of(databasesDomain.getId(), softwareDomain.getId())
            )
        );
        SubjectModuleResponse algorithms = subjectModuleService.createSubjectModule(
            root,
            firstSemester.getId(),
            new CreateSubjectModuleRequest("ALG101", "Algorithms")
        );
        SubjectModuleResponse advancedDatabases = subjectModuleService.createSubjectModule(
            root,
            firstSemester.getId(),
            new CreateSubjectModuleRequest("DB201", "Databases")
        );

        assertThat(databases.code()).isEqualTo("DB101");
        assertThat(databases.semesterId()).isEqualTo(firstSemester.getId());
        assertThat(databases.academicDomainIds())
            .containsExactlyInAnyOrder(databasesDomain.getId(), softwareDomain.getId());
        assertThat(subjectModuleService.getSubjectModules(root, firstSemester.getId()))
            .extracting(SubjectModuleResponse::id)
            .containsExactly(algorithms.id(), databases.id(), advancedDatabases.id());
        assertThat(subjectModuleService.getSubjectModule(root, databases.id()).title())
            .isEqualTo("Databases");

        assertThatThrownBy(() -> subjectModuleService.createSubjectModule(
            root,
            firstSemester.getId(),
            new CreateSubjectModuleRequest("db101", "Another title")
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");

        assertThatThrownBy(() -> subjectModuleService.createSubjectModule(
            root,
            firstSemester.getId(),
            new CreateSubjectModuleRequest(
                "MATH101",
                "Mathematics",
                Set.of(mathematicsDomain.getId())
            )
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");

        SubjectModuleResponse sameCodeInAnotherSemester = subjectModuleService.createSubjectModule(
            root,
            secondSemester.getId(),
            new CreateSubjectModuleRequest("DB101", "Databases")
        );
        assertThat(sameCodeInAnotherSemester.semesterId()).isEqualTo(secondSemester.getId());

        SubjectModuleResponse updated = subjectModuleService.updateSubjectModule(
            root,
            databases.id(),
            new UpdateSubjectModuleRequest(
                "DB102",
                "Relational Databases",
                Set.of(databasesDomain.getId())
            )
        );
        assertThat(updated.code()).isEqualTo("DB102");
        assertThat(updated.title()).isEqualTo("Relational Databases");
        assertThat(updated.academicDomainIds()).containsExactly(databasesDomain.getId());

        assertThat(subjectModuleService.deleteSubjectModule(root, updated.id()).success()).isTrue();
        assertThat(subjectModuleRepository.findById(updated.id())).isEmpty();
    }

    @Test
    void adminNeedsTheMatchingPermissionAndEstablishment() {
        AuthenticatedUserPrincipal root = principal(AccountRoleType.ROOT_SUPER_ADMIN, UUID.randomUUID(), null);
        SubjectModuleResponse firstModule = subjectModuleService.createSubjectModule(
            root,
            firstSemester.getId(),
            new CreateSubjectModuleRequest("ALG101", "Algorithms")
        );
        SubjectModuleResponse secondModule = subjectModuleService.createSubjectModule(
            root,
            secondSemester.getId(),
            new CreateSubjectModuleRequest("ALG101", "Algorithms")
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
            .findByCodeIn(Set.of(PermissionCode.SUBJECT_MODULE_UPDATE))
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

        assertThat(subjectModuleService.updateSubjectModule(
            adminPrincipal,
            firstModule.id(),
            new UpdateSubjectModuleRequest("ALG102", "Advanced Algorithms")
        ).code()).isEqualTo("ALG102");
        assertThatThrownBy(() -> subjectModuleService.getSubjectModule(adminPrincipal, firstModule.id()))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
        assertThatThrownBy(() -> subjectModuleService.updateSubjectModule(
            adminPrincipal,
            secondModule.id(),
            new UpdateSubjectModuleRequest("DENIED", "Denied")
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
    }

    private Semester saveSemester(
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
        level = academicLevelRepository.save(level);

        AcademicYear academicYear = new AcademicYear();
        academicYear.setEstablishment(establishment);
        academicYear.setLabel("2026-2027");
        academicYear.setStartYear(2026);
        academicYear.setEndYear(2027);
        academicYear.setStatus(AcademicYearStatus.ACTIVE);
        academicYear = academicYearRepository.save(academicYear);

        Semester semester = new Semester();
        semester.setAcademicLevel(level);
        semester.setAcademicYear(academicYear);
        semester.setName("S1");
        semester.setSemesterOrder(1);
        return semesterRepository.save(semester);
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

    private AcademicDomain saveAcademicDomain(
        Establishment establishment,
        String code,
        String name
    ) {
        AcademicDomain academicDomain = new AcademicDomain();
        academicDomain.setEstablishment(establishment);
        academicDomain.setCode(code);
        academicDomain.setName(name);
        return academicDomainRepository.save(academicDomain);
    }

    private void clearBusinessData() {
        adminPermissionGrantRepository.deleteAll();
        subjectModuleDomainRepository.deleteAll();
        subjectModuleRepository.deleteAll();
        academicDomainRepository.deleteAll();
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
