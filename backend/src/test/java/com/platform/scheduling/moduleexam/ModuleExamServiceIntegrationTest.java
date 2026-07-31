package com.platform.scheduling.moduleexam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.Admin;
import com.platform.identityaccess.domain.AdminPermissionGrant;
import com.platform.identityaccess.domain.Permission;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.Professor;
import com.platform.identityaccess.domain.UserAccount;
import com.platform.identityaccess.infrastructure.AdminPermissionGrantRepository;
import com.platform.identityaccess.infrastructure.AdminRepository;
import com.platform.identityaccess.infrastructure.PermissionRepository;
import com.platform.identityaccess.infrastructure.ProfessorRepository;
import com.platform.identityaccess.infrastructure.RootSuperAdminRepository;
import com.platform.identityaccess.infrastructure.StudentRepository;
import com.platform.identityaccess.infrastructure.SuperAdminRepository;
import com.platform.identityaccess.infrastructure.UserAccountRepository;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.PlatformApplication;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.examschedule.application.ExamScheduleService;
import com.platform.scheduling.examschedule.domain.ExamSessionType;
import com.platform.scheduling.examschedule.infrastructure.ExamScheduleRepository;
import com.platform.scheduling.examschedule.presentation.dto.CreateExamSchedule;
import com.platform.scheduling.examschedule.presentation.dto.ExamScheduleResponse;
import com.platform.scheduling.moduleexam.application.ModuleExamService;
import com.platform.scheduling.moduleexam.infrastructure.ModuleExamRepository;
import com.platform.scheduling.moduleexam.presentation.dto.CreateModuleExamRequest;
import com.platform.scheduling.moduleexam.presentation.dto.ModuleExamResponse;
import com.platform.scheduling.moduleexam.presentation.dto.UpdateModuleExamRequest;
import com.platform.moduleclassresponsibility.domain.ModuleClassResponsibility;
import com.platform.moduleclassresponsibility.domain.ModuleClassResponsibilityStatus;
import com.platform.moduleclassresponsibility.infrastructure.ModuleClassResponsibilityRepository;
import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academiclevel.infrastructure.AcademicLevelRepository;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicyear.domain.AcademicYearStatus;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.classgroup.domain.ClassGroup;
import com.platform.universitygovernance.classgroup.domain.ClassGroupStatus;
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
import com.platform.universitygovernance.subjectmodules.domain.SubjectModule;
import com.platform.universitygovernance.subjectmodules.infrastructure.SubjectModuleRepository;
import com.platform.universitygovernance.university.domain.University;
import com.platform.universitygovernance.university.infrastructure.UniversityRepository;
import java.time.LocalDate;
import java.time.LocalTime;
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
class ModuleExamServiceIntegrationTest {

    @Autowired
    private ModuleExamService moduleExamService;

    @Autowired
    private ExamScheduleService examScheduleService;

    @Autowired
    private ModuleExamRepository moduleExamRepository;

    @Autowired
    private ExamScheduleRepository examScheduleRepository;

    @Autowired
    private ModuleClassResponsibilityRepository responsibilityRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private AdminPermissionGrantRepository adminPermissionGrantRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ProfessorRepository professorRepository;

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
    private SubjectModuleRepository subjectModuleRepository;

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
    private EstablishmentRepository establishmentRepository;

    @Autowired
    private UniversityRepository universityRepository;

    private Establishment establishment;
    private AcademicYear academicYear;
    private Semester semester;
    private AcademicLevel academicLevel;
    private ClassGroup classGroup;
    private SubjectModule algorithms;
    private ModuleClassResponsibility algorithmsAssignment;
    private AuthenticatedUserPrincipal root;
    private ExamScheduleResponse examSchedule;

    @BeforeEach
    void setUp() {
        clearBusinessData();

        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);

        establishment = saveEstablishment(university, "ENSA Agadir");
        ProgramFiliere program = saveProgram(establishment, "IL");
        academicLevel = saveLevel(program, "M1");
        academicYear = saveYear(establishment, "2026-2027", 2026);
        semester = saveSemester(academicLevel, academicYear, "S1");
        classGroup = saveClassGroup("Group A");
        algorithms = saveSubjectModule("ALG", "Algorithms");
        Professor professor = saveProfessor("professor@ensa.uiz.ac.ma");
        algorithmsAssignment = saveResponsibility(
            professor,
            algorithms,
            classGroup
        );

        root = principal(AccountRoleType.ROOT_SUPER_ADMIN, UUID.randomUUID(), null);
        examSchedule = examScheduleService.createExamSchedule(
            root,
            establishment.getId(),
            new CreateExamSchedule(
                academicYear.getId(),
                semester.getId(),
                ExamSessionType.NORMAL
            )
        );
    }

    @AfterEach
    void tearDown() {
        clearBusinessData();
    }

    @Test
    void rootCanCreateReadUpdateAndDeleteModuleExam() {
        ModuleExamResponse created = moduleExamService.createModuleExam(
            root,
            examSchedule.id(),
            createRequest(
                algorithms,
                algorithmsAssignment,
                LocalDate.of(2027, 1, 10),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                " Room A "
            )
        );

        assertThat(created.location()).isEqualTo("Room A");
        assertThat(moduleExamService.getModuleExam(root, created.id()).id())
            .isEqualTo(created.id());
        assertThat(moduleExamService.getModuleExams(root, examSchedule.id()))
            .extracting(ModuleExamResponse::id)
            .containsExactly(created.id());

        ModuleExamResponse updated = moduleExamService.updateModuleExam(
            root,
            created.id(),
            new UpdateModuleExamRequest(
                algorithms.getId(),
                classGroup.getId(),
                LocalDate.of(2027, 1, 11),
                LocalTime.of(10, 0),
                null,
                "Room B"
            )
        );

        assertThat(updated.examDate()).isEqualTo(LocalDate.of(2027, 1, 11));
        assertThat(updated.endTime()).isNull();
        assertThat(moduleExamService.deleteModuleExam(root, created.id()).success())
            .isTrue();
        assertThat(moduleExamRepository.findById(created.id())).isEmpty();
    }

    @Test
    void duplicateAndOverlappingGroupExamsAreRejected() {
        LocalDate examDate = LocalDate.of(2027, 1, 10);
        moduleExamService.createModuleExam(
            root,
            examSchedule.id(),
            createRequest(
                algorithms,
                algorithmsAssignment,
                examDate,
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                null
            )
        );

        assertThatThrownBy(() -> moduleExamService.createModuleExam(
            root,
            examSchedule.id(),
            createRequest(
                algorithms,
                algorithmsAssignment,
                LocalDate.of(2027, 1, 12),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                null
            )
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");

        SubjectModule databases = saveSubjectModule("DB", "Databases");
        ModuleClassResponsibility databasesAssignment = saveResponsibility(
            algorithmsAssignment.getProfessor(),
            databases,
            classGroup
        );

        assertThatThrownBy(() -> moduleExamService.createModuleExam(
            root,
            examSchedule.id(),
            createRequest(
                databases,
                databasesAssignment,
                examDate,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                null
            )
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");

        assertThatThrownBy(() -> moduleExamService.createModuleExam(
            root,
            examSchedule.id(),
            createRequest(
                databases,
                databasesAssignment,
                LocalDate.of(2027, 1, 11),
                LocalTime.of(12, 0),
                LocalTime.of(11, 0),
                null
            )
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");

        examScheduleService.deleteExamSchedule(root, examSchedule.id());
        assertThat(moduleExamRepository.count()).isZero();
    }

    @Test
    void publicationFreezesModuleExamsAndAdminPermissionsRemainIndependent() {
        Admin admin = saveAdmin(establishment);
        grant(admin, PermissionCode.EXAM_SCHEDULE_UPDATE);
        AuthenticatedUserPrincipal adminPrincipal = principal(
            AccountRoleType.ADMIN,
            admin.getId(),
            establishment.getId()
        );

        ModuleExamResponse created = moduleExamService.createModuleExam(
            adminPrincipal,
            examSchedule.id(),
            createRequest(
                algorithms,
                algorithmsAssignment,
                LocalDate.of(2027, 1, 10),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                null
            )
        );

        assertThatThrownBy(() -> moduleExamService.getModuleExam(
            adminPrincipal,
            created.id()
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");

        grant(admin, PermissionCode.EXAM_SCHEDULE_VIEW);
        assertThat(moduleExamService.getModuleExam(adminPrincipal, created.id()).id())
            .isEqualTo(created.id());

        examScheduleService.publishExamSchedule(root, examSchedule.id());
        assertThatThrownBy(() -> moduleExamService.deleteModuleExam(
            adminPrincipal,
            created.id()
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");
    }

    private CreateModuleExamRequest createRequest(
        SubjectModule subjectModule,
        ModuleClassResponsibility responsibility,
        LocalDate examDate,
        LocalTime startTime,
        LocalTime endTime,
        String location
    ) {
        return new CreateModuleExamRequest(
            subjectModule.getId(),
            classGroup.getId(),
            examDate,
            startTime,
            endTime,
            location
        );
    }

    private Professor saveProfessor(String email) {
        UserAccount account = new UserAccount();
        account.setUniversityEmail(email);
        account.setPasswordHash("not-used-by-this-test");
        account.setRole(AccountRoleType.PROFESSOR);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account = userAccountRepository.save(account);

        Professor professor = new Professor();
        professor.setUserAccount(account);
        professor.setEstablishment(establishment);
        professor.setEmployeeNumber("EMP-" + UUID.randomUUID());
        professor.setMaximumWeeklyTeachingMinutes(480);
        return professorRepository.save(professor);
    }

    private Admin saveAdmin(Establishment adminEstablishment) {
        UserAccount account = new UserAccount();
        account.setUniversityEmail("admin@ensa.uiz.ac.ma");
        account.setPasswordHash("not-used-by-this-test");
        account.setRole(AccountRoleType.ADMIN);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account = userAccountRepository.save(account);

        Admin admin = new Admin();
        admin.setUserAccount(account);
        admin.setEstablishment(adminEstablishment);
        return adminRepository.save(admin);
    }

    private void grant(Admin admin, PermissionCode code) {
        Permission permission = permissionRepository.findByCodeIn(Set.of(code)).get(0);
        AdminPermissionGrant grant = new AdminPermissionGrant();
        grant.setAdmin(admin);
        grant.setPermission(permission);
        adminPermissionGrantRepository.save(grant);
    }

    private ModuleClassResponsibility saveResponsibility(
        Professor professor,
        SubjectModule subjectModule,
        ClassGroup assignmentClassGroup
    ) {
        ModuleClassResponsibility responsibility = new ModuleClassResponsibility();
        responsibility.setProfessor(professor);
        responsibility.setSubjectModule(subjectModule);
        responsibility.setClassGroup(assignmentClassGroup);
        responsibility.setAcademicYear(academicYear);
        responsibility.setSemester(semester);
        responsibility.setStatus(ModuleClassResponsibilityStatus.ACTIVE);
        return responsibilityRepository.save(responsibility);
    }

    private SubjectModule saveSubjectModule(String code, String title) {
        SubjectModule subjectModule = new SubjectModule();
        subjectModule.setSemester(semester);
        subjectModule.setCode(code);
        subjectModule.setTitle(title);
        return subjectModuleRepository.save(subjectModule);
    }

    private ClassGroup saveClassGroup(String name) {
        ClassGroup savedClassGroup = new ClassGroup();
        savedClassGroup.setAcademicLevel(academicLevel);
        savedClassGroup.setAcademicYear(academicYear);
        savedClassGroup.setName(name);
        savedClassGroup.setStatus(ClassGroupStatus.ACTIVE);
        return classGroupRepository.save(savedClassGroup);
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
        Establishment savedEstablishment = new Establishment();
        savedEstablishment.setUniversity(university);
        savedEstablishment.setName(name);
        savedEstablishment.setEstablishmentType(EstablishmentType.SCHOOL);
        savedEstablishment.setEstablishmentStatus(EstablishmentStatus.ACTIVE);
        return establishmentRepository.save(savedEstablishment);
    }

    private ProgramFiliere saveProgram(Establishment programEstablishment, String code) {
        Department department = new Department();
        department.setEstablishment(programEstablishment);
        department.setName(code + " Department");
        department = departmentRepository.save(department);

        DegreeCycle degreeCycle = new DegreeCycle();
        degreeCycle.setEstablishment(programEstablishment);
        degreeCycle.setName(code + " Cycle");
        degreeCycle = degreeCycleRepository.save(degreeCycle);

        ProgramPath programPath = new ProgramPath();
        programPath.setEstablishment(programEstablishment);
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
        Establishment yearEstablishment,
        String label,
        int startYear
    ) {
        AcademicYear year = new AcademicYear();
        year.setEstablishment(yearEstablishment);
        year.setLabel(label);
        year.setStartYear(startYear);
        year.setEndYear(startYear + 1);
        year.setStatus(AcademicYearStatus.ACTIVE);
        return academicYearRepository.save(year);
    }

    private Semester saveSemester(
        AcademicLevel level,
        AcademicYear year,
        String name
    ) {
        Semester savedSemester = new Semester();
        savedSemester.setAcademicLevel(level);
        savedSemester.setAcademicYear(year);
        savedSemester.setName(name);
        savedSemester.setSemesterOrder(1);
        return semesterRepository.save(savedSemester);
    }

    private void clearBusinessData() {
        moduleExamRepository.deleteAll();
        examScheduleRepository.deleteAll();
        responsibilityRepository.deleteAll();
        adminPermissionGrantRepository.deleteAll();
        subjectModuleRepository.deleteAll();
        classGroupRepository.deleteAll();
        professorRepository.deleteAll();
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
