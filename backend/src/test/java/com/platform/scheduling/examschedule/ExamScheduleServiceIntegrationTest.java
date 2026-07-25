package com.platform.scheduling.examschedule;

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
import com.platform.identityaccess.infrastructure.StudentRepository;
import com.platform.identityaccess.infrastructure.SuperAdminRepository;
import com.platform.identityaccess.infrastructure.UserAccountRepository;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.PlatformApplication;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.examschedule.application.ExamScheduleService;
import com.platform.scheduling.examschedule.domain.ExamSessionType;
import com.platform.scheduling.examschedule.domain.PublicationStatus;
import com.platform.scheduling.examschedule.infrastructure.ExamScheduleRepository;
import com.platform.scheduling.examschedule.presentation.dto.CreateExamSchedule;
import com.platform.scheduling.examschedule.presentation.dto.ExamScheduleResponse;
import com.platform.scheduling.examschedule.presentation.dto.UpdateExamScheduleRequest;
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
import com.platform.universitygovernance.semester.domain.Semester;
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
class ExamScheduleServiceIntegrationTest {

    @Autowired
    private ExamScheduleService examScheduleService;

    @Autowired
    private ExamScheduleRepository examScheduleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private AdminPermissionGrantRepository adminPermissionGrantRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private RootSuperAdminRepository rootSuperAdminRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

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
    private EstablishmentRepository establishmentRepository;

    @Autowired
    private UniversityRepository universityRepository;

    private Establishment firstEstablishment;
    private Establishment secondEstablishment;
    private AcademicYear academicYear;
    private Semester semester;

    @BeforeEach
    void setUp() {
        clearBusinessData();

        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);

        firstEstablishment = saveEstablishment(university, "ENSA Agadir");
        secondEstablishment = saveEstablishment(university, "Faculty of Sciences");
        ProgramFiliere program = saveProgram(firstEstablishment, "IL");
        AcademicLevel academicLevel = saveLevel(program, "M1");
        academicYear = saveYear(firstEstablishment, "2026-2027", 2026);
        semester = saveSemester(academicLevel, academicYear, "S1");
    }

    @AfterEach
    void tearDown() {
        clearBusinessData();
    }

    @Test
    void rootCanManageDraftAndPublishExamSchedule() {
        AuthenticatedUserPrincipal root = principal(
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null
        );

        ExamScheduleResponse created = examScheduleService.createExamSchedule(
            root,
            firstEstablishment.getId(),
            request(ExamSessionType.NORMAL)
        );

        assertThat(created.publicationStatus()).isEqualTo(PublicationStatus.DRAFT);
        assertThat(examScheduleService.getExamSchedule(root, created.id()).id())
            .isEqualTo(created.id());
        assertThat(examScheduleService.getExamSchedules(root, firstEstablishment.getId()))
            .extracting(ExamScheduleResponse::id)
            .containsExactly(created.id());

        ExamScheduleResponse updated = examScheduleService.updateExamSchedule(
            root,
            created.id(),
            new UpdateExamScheduleRequest(
                academicYear.getId(),
                semester.getId(),
                ExamSessionType.RATTRAPAGE
            )
        );
        assertThat(updated.sessionType()).isEqualTo(ExamSessionType.RATTRAPAGE);

        ExamScheduleResponse published = examScheduleService.publishExamSchedule(
            root,
            created.id()
        );
        assertThat(published.publicationStatus()).isEqualTo(PublicationStatus.PUBLISHED);
        assertThat(examScheduleService.publishExamSchedule(root, created.id())
            .publicationStatus()).isEqualTo(PublicationStatus.PUBLISHED);

        assertThatThrownBy(() -> examScheduleService.updateExamSchedule(
            root,
            created.id(),
            new UpdateExamScheduleRequest(
                academicYear.getId(),
                semester.getId(),
                ExamSessionType.NORMAL
            )
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");

        assertThatThrownBy(() -> examScheduleService.deleteExamSchedule(
            root,
            created.id()
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");
    }

    @Test
    void normalAndRattrapageAreIndependentButDuplicateContextIsRejected() {
        AuthenticatedUserPrincipal root = principal(
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null
        );

        ExamScheduleResponse normal = examScheduleService.createExamSchedule(
            root,
            firstEstablishment.getId(),
            request(ExamSessionType.NORMAL)
        );
        ExamScheduleResponse rattrapage = examScheduleService.createExamSchedule(
            root,
            firstEstablishment.getId(),
            request(ExamSessionType.RATTRAPAGE)
        );

        assertThat(normal.id()).isNotEqualTo(rattrapage.id());
        assertThatThrownBy(() -> examScheduleService.createExamSchedule(
            root,
            firstEstablishment.getId(),
            request(ExamSessionType.NORMAL)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");

        assertThatThrownBy(() -> examScheduleService.createExamSchedule(
            root,
            secondEstablishment.getId(),
            request(ExamSessionType.NORMAL)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");

        assertThat(examScheduleService.deleteExamSchedule(root, normal.id()).success())
            .isTrue();
        assertThat(examScheduleRepository.findById(normal.id())).isEmpty();
    }

    @Test
    void adminNeedsIndependentPermissionsInTheMatchingEstablishment() {
        Admin admin = saveAdmin(firstEstablishment);
        grant(admin, PermissionCode.EXAM_SCHEDULE_CREATE);
        AuthenticatedUserPrincipal principal = principal(
            AccountRoleType.ADMIN,
            admin.getId(),
            firstEstablishment.getId()
        );

        ExamScheduleResponse created = examScheduleService.createExamSchedule(
            principal,
            firstEstablishment.getId(),
            request(ExamSessionType.NORMAL)
        );

        assertThatThrownBy(() -> examScheduleService.getExamSchedule(
            principal,
            created.id()
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");

        grant(admin, PermissionCode.EXAM_SCHEDULE_VIEW);
        assertThat(examScheduleService.getExamSchedule(principal, created.id()).id())
            .isEqualTo(created.id());

        assertThatThrownBy(() -> examScheduleService.publishExamSchedule(
            principal,
            created.id()
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");

        assertThatThrownBy(() -> examScheduleService.getExamSchedules(
            principal,
            secondEstablishment.getId()
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
    }

    private CreateExamSchedule request(ExamSessionType sessionType) {
        return new CreateExamSchedule(
            academicYear.getId(),
            semester.getId(),
            sessionType
        );
    }

    private Admin saveAdmin(Establishment establishment) {
        UserAccount account = new UserAccount();
        account.setUniversityEmail("admin@ensa.uiz.ac.ma");
        account.setPasswordHash("not-used-by-this-test");
        account.setRole(AccountRoleType.ADMIN);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account = userAccountRepository.save(account);

        Admin admin = new Admin();
        admin.setUserAccount(account);
        admin.setEstablishment(establishment);
        return adminRepository.save(admin);
    }

    private void grant(Admin admin, PermissionCode code) {
        Permission permission = permissionRepository.findByCodeIn(Set.of(code)).get(0);
        AdminPermissionGrant grant = new AdminPermissionGrant();
        grant.setAdmin(admin);
        grant.setPermission(permission);
        adminPermissionGrantRepository.save(grant);
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

    private Establishment saveEstablishment(University university, String name) {
        Establishment establishment = new Establishment();
        establishment.setUniversity(university);
        establishment.setName(name);
        establishment.setEstablishmentType(EstablishmentType.SCHOOL);
        establishment.setEstablishmentStatus(EstablishmentStatus.ACTIVE);
        return establishmentRepository.save(establishment);
    }

    private ProgramFiliere saveProgram(Establishment establishment, String code) {
        Department department = new Department();
        department.setEstablishment(establishment);
        department.setName(code + " Department");
        department = departmentRepository.save(department);

        DegreeCycle degreeCycle = new DegreeCycle();
        degreeCycle.setEstablishment(establishment);
        degreeCycle.setName(code + " Cycle");
        degreeCycle = degreeCycleRepository.save(degreeCycle);

        ProgramPath programPath = new ProgramPath();
        programPath.setEstablishment(establishment);
        programPath.setName(code + " Path");
        programPath = programPathRepository.save(programPath);

        ProgramFiliere program = new ProgramFiliere();
        program.setDepartment(department);
        program.setDegreeCycle(degreeCycle);
        program.setProgramPath(programPath);
        program.setCode(code);
        program.setName(code + " Program");
        return programFiliereRepository.save(program);
    }

    private AcademicLevel saveLevel(ProgramFiliere program, String name) {
        AcademicLevel level = new AcademicLevel();
        level.setProgramFiliere(program);
        level.setName(name);
        level.setLevelOrder(1);
        return academicLevelRepository.save(level);
    }

    private AcademicYear saveYear(
        Establishment establishment,
        String label,
        int startYear
    ) {
        AcademicYear year = new AcademicYear();
        year.setEstablishment(establishment);
        year.setLabel(label);
        year.setStartYear(startYear);
        year.setEndYear(startYear + 1);
        year.setStatus(AcademicYearStatus.ACTIVE);
        return academicYearRepository.save(year);
    }

    private Semester saveSemester(
        AcademicLevel academicLevel,
        AcademicYear year,
        String name
    ) {
        Semester savedSemester = new Semester();
        savedSemester.setAcademicLevel(academicLevel);
        savedSemester.setAcademicYear(year);
        savedSemester.setName(name);
        savedSemester.setSemesterOrder(1);
        return semesterRepository.save(savedSemester);
    }

    private void clearBusinessData() {
        examScheduleRepository.deleteAll();
        adminPermissionGrantRepository.deleteAll();
        studentRepository.deleteAll();
        adminRepository.deleteAll();
        superAdminRepository.deleteAll();
        rootSuperAdminRepository.deleteAll();
        semesterRepository.deleteAll();
        academicLevelRepository.deleteAll();
        academicYearRepository.deleteAll();
        programFiliereRepository.deleteAll();
        degreeCycleRepository.deleteAll();
        programPathRepository.deleteAll();
        departmentRepository.deleteAll();
        establishmentRepository.deleteAll();
        universityRepository.deleteAll();
        userProfileRepository.deleteAll();
        userAccountRepository.deleteAll();
    }
}
