package com.platform.assessment.graderecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.platform.academicregistration.classassignment.application.StudentClassAssignmentService;
import com.platform.academicregistration.classassignment.infrastructure.StudentClassAssignmentRepository;
import com.platform.academicregistration.classassignment.presentation.dto.AssignStudentClassRequest;
import com.platform.academicregistration.registration.domain.AcademicRegistration;
import com.platform.academicregistration.registration.domain.AcademicRegistrationStatus;
import com.platform.academicregistration.registration.infrastructure.AcademicRegistrationRepository;
import com.platform.academicregistration.semesterregistration.domain.SemesterRegestration;
import com.platform.academicregistration.semesterregistration.infrastructer.SemesterRegestrationRepository;
import com.platform.academicregistration.subjectmoduleregestration.domain.SubjectModuleRegestration;
import com.platform.academicregistration.subjectmoduleregestration.domain.SubjectModuleRegistrationStatus;
import com.platform.academicregistration.subjectmoduleregestration.infrastructure.SubjectRegestrationRepository;
import com.platform.assessment.graderecord.application.GradeRecordService;
import com.platform.assessment.graderecord.domain.GradeWorkflowStatus;
import com.platform.assessment.graderecord.domain.ZeroGradeReason;
import com.platform.assessment.graderecord.infrastructure.GradeRecordRepository;
import com.platform.assessment.graderecord.presentation.dto.GradeItemRequest;
import com.platform.assessment.graderecord.presentation.dto.GradeSheetResponse;
import com.platform.assessment.graderecord.presentation.dto.SaveGradeSheetRequest;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.Admin;
import com.platform.identityaccess.domain.AdminPermissionGrant;
import com.platform.identityaccess.domain.Permission;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.Professor;
import com.platform.identityaccess.domain.Student;
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
import com.platform.teachingassignment.domain.TeachingAssignment;
import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import com.platform.teachingassignment.infrastructure.TeachingAssignmentRepository;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
class GradeRecordServiceIntegrationTest {

    @Autowired private GradeRecordService gradeRecordService;
    @Autowired private StudentClassAssignmentService classAssignmentService;
    @Autowired private ExamScheduleService examScheduleService;
    @Autowired private ModuleExamService moduleExamService;
    @Autowired private GradeRecordRepository gradeRecordRepository;
    @Autowired private StudentClassAssignmentRepository classAssignmentRepository;
    @Autowired private ModuleExamRepository moduleExamRepository;
    @Autowired private ExamScheduleRepository examScheduleRepository;
    @Autowired private TeachingAssignmentRepository teachingAssignmentRepository;
    @Autowired private SubjectRegestrationRepository moduleRegistrationRepository;
    @Autowired private SemesterRegestrationRepository semesterRegistrationRepository;
    @Autowired private AcademicRegistrationRepository academicRegistrationRepository;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private AdminPermissionGrantRepository adminPermissionGrantRepository;
    @Autowired private AdminRepository adminRepository;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private SuperAdminRepository superAdminRepository;
    @Autowired private RootSuperAdminRepository rootSuperAdminRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private UserAccountRepository userAccountRepository;
    @Autowired private SubjectModuleRepository subjectModuleRepository;
    @Autowired private ClassGroupRepository classGroupRepository;
    @Autowired private SemesterRepository semesterRepository;
    @Autowired private AcademicLevelRepository academicLevelRepository;
    @Autowired private AcademicYearRepository academicYearRepository;
    @Autowired private ProgramFiliereRepository programFiliereRepository;
    @Autowired private DegreeCycleRepository degreeCycleRepository;
    @Autowired private ProgramPathRepository programPathRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private EstablishmentRepository establishmentRepository;
    @Autowired private UniversityRepository universityRepository;

    private Establishment establishment;
    private ProgramFiliere program;
    private AcademicLevel academicLevel;
    private AcademicYear academicYear;
    private Semester semester;
    private SubjectModule subjectModule;
    private ClassGroup classGroup;
    private Professor professor;
    private TeachingAssignment teachingAssignment;
    private ModuleExamResponse moduleExam;
    private SubjectModuleRegestration firstModuleRegistration;
    private SubjectModuleRegestration secondModuleRegistration;
    private Student firstStudent;
    private AuthenticatedUserPrincipal root;
    private AuthenticatedUserPrincipal professorPrincipal;

    @BeforeEach
    void setUp() {
        clearBusinessData();

        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);
        establishment = saveEstablishment(university);
        program = saveProgram(establishment);
        academicLevel = saveLevel(program);
        academicYear = saveYear(establishment);
        semester = saveSemester(academicLevel, academicYear);
        subjectModule = saveSubjectModule(semester);
        classGroup = saveClassGroup(academicLevel, academicYear);
        professor = saveProfessor();
        teachingAssignment = saveTeachingAssignment();

        root = principal(AccountRoleType.ROOT_SUPER_ADMIN, UUID.randomUUID(), null);
        professorPrincipal = principal(
            AccountRoleType.PROFESSOR,
            professor.getId(),
            establishment.getId()
        );

        firstStudent = saveStudent("student1@uiz.ac.ma");
        Student secondStudent = saveStudent("student2@uiz.ac.ma");
        firstModuleRegistration = registerStudent(firstStudent);
        secondModuleRegistration = registerStudent(secondStudent);

        ExamScheduleResponse schedule = examScheduleService.createExamSchedule(
            root,
            establishment.getId(),
            new CreateExamSchedule(
                academicYear.getId(),
                semester.getId(),
                ExamSessionType.NORMAL
            )
        );
        moduleExam = moduleExamService.createModuleExam(
            root,
            schedule.id(),
            new CreateModuleExamRequest(
                subjectModule.getId(),
                classGroup.getId(),
                teachingAssignment.getId(),
                LocalDate.of(2027, 1, 10),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                "Room A"
            )
        );
        examScheduleService.publishExamSchedule(root, schedule.id());
    }

    @AfterEach
    void tearDown() {
        clearBusinessData();
    }

    @Test
    void assignedProfessorAndManagementCanCompleteGradeWorkflow() {
        GradeSheetResponse emptySheet = gradeRecordService.getGradeSheet(
            professorPrincipal,
            moduleExam.id()
        );
        assertThat(emptySheet.workflowStatus()).isEqualTo(GradeWorkflowStatus.DRAFT);
        assertThat(emptySheet.grades()).hasSize(2);

        GradeSheetResponse draft = gradeRecordService.saveDraftGradeSheet(
            professorPrincipal,
            moduleExam.id(),
            completeGradeRequest()
        );
        assertThat(draft.grades())
            .extracting(item -> item.gradeValue())
            .containsExactlyInAnyOrder(new BigDecimal("14.50"), new BigDecimal("0.00"));
        assertThat(draft.grades())
            .filteredOn(item -> item.gradeValue().signum() == 0)
            .singleElement()
            .extracting(item -> item.zeroGradeReason())
            .isEqualTo(ZeroGradeReason.ABSENT);

        AuthenticatedUserPrincipal studentPrincipal = principal(
            AccountRoleType.STUDENT,
            firstStudent.getId(),
            establishment.getId()
        );
        assertThat(gradeRecordService.getMyGrades(
            studentPrincipal,
            null,
            null,
            null
        )).isEmpty();

        assertThat(gradeRecordService.submitGradeSheet(
            professorPrincipal,
            moduleExam.id()
        ).workflowStatus()).isEqualTo(GradeWorkflowStatus.SUBMITTED);
        assertThat(gradeRecordService.reviewGradeSheet(root, moduleExam.id())
            .workflowStatus()).isEqualTo(GradeWorkflowStatus.REVIEWED);
        assertThat(gradeRecordService.approveGradeSheet(root, moduleExam.id())
            .workflowStatus()).isEqualTo(GradeWorkflowStatus.APPROVED);
        assertThat(gradeRecordService.publishGradeSheet(root, moduleExam.id())
            .workflowStatus()).isEqualTo(GradeWorkflowStatus.PUBLISHED);

        assertThat(gradeRecordService.getMyGrades(
            studentPrincipal,
            academicYear.getId(),
            academicLevel.getId(),
            semester.getId()
        ))
            .singleElement()
            .satisfies(grade -> {
                assertThat(grade.moduleRegistrationId())
                    .isEqualTo(firstModuleRegistration.getId());
                assertThat(grade.gradeValue()).isEqualByComparingTo("14.50");
                assertThat(grade.publishedAt()).isNotNull();
            });
        assertThat(gradeRecordService.getStudentGrades(
            root,
            firstStudent.getId(),
            null,
            academicLevel.getId(),
            null
        )).hasSize(1);

        assertThatThrownBy(() -> gradeRecordService.saveDraftGradeSheet(
            professorPrincipal,
            moduleExam.id(),
            completeGradeRequest()
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");
    }

    @Test
    void gradeSheetRequiresAssignedProfessorCompleteEligibilityAndAdminPermission() {
        Professor otherProfessor = saveProfessor("other-professor@uiz.ac.ma");
        AuthenticatedUserPrincipal otherProfessorPrincipal = principal(
            AccountRoleType.PROFESSOR,
            otherProfessor.getId(),
            establishment.getId()
        );
        assertThatThrownBy(() -> gradeRecordService.saveDraftGradeSheet(
            otherProfessorPrincipal,
            moduleExam.id(),
            completeGradeRequest()
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");

        SaveGradeSheetRequest incomplete = new SaveGradeSheetRequest(List.of(
            new GradeItemRequest(
                firstModuleRegistration.getId(),
                new BigDecimal("14.50"),
                null
            )
        ));
        assertThatThrownBy(() -> gradeRecordService.saveDraftGradeSheet(
            professorPrincipal,
            moduleExam.id(),
            incomplete
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");

        SaveGradeSheetRequest zeroWithoutReason = new SaveGradeSheetRequest(List.of(
            new GradeItemRequest(
                firstModuleRegistration.getId(),
                new BigDecimal("14.50"),
                null
            ),
            new GradeItemRequest(
                secondModuleRegistration.getId(),
                BigDecimal.ZERO,
                null
            )
        ));
        assertThatThrownBy(() -> gradeRecordService.saveDraftGradeSheet(
            professorPrincipal,
            moduleExam.id(),
            zeroWithoutReason
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");

        gradeRecordService.saveDraftGradeSheet(
            professorPrincipal,
            moduleExam.id(),
            completeGradeRequest()
        );
        gradeRecordService.submitGradeSheet(professorPrincipal, moduleExam.id());

        Admin admin = saveAdmin();
        AuthenticatedUserPrincipal adminPrincipal = principal(
            AccountRoleType.ADMIN,
            admin.getId(),
            establishment.getId()
        );
        assertThatThrownBy(() -> gradeRecordService.reviewGradeSheet(
            adminPrincipal,
            moduleExam.id()
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");

        grant(admin, PermissionCode.GRADE_REVIEW);
        assertThat(gradeRecordService.reviewGradeSheet(adminPrincipal, moduleExam.id())
            .workflowStatus()).isEqualTo(GradeWorkflowStatus.REVIEWED);
    }

    private SaveGradeSheetRequest completeGradeRequest() {
        return new SaveGradeSheetRequest(List.of(
            new GradeItemRequest(
                firstModuleRegistration.getId(),
                new BigDecimal("14.50"),
                null
            ),
            new GradeItemRequest(
                secondModuleRegistration.getId(),
                new BigDecimal("0.00"),
                ZeroGradeReason.ABSENT
            )
        ));
    }

    private SubjectModuleRegestration registerStudent(Student student) {
        AcademicRegistration registration = new AcademicRegistration();
        registration.setStudent(student);
        registration.setProgramFiliere(program);
        registration.setAcademicLevel(academicLevel);
        registration.setAcademicYear(academicYear);
        registration.setStatus(AcademicRegistrationStatus.ACTIVE);
        registration = academicRegistrationRepository.save(registration);

        SemesterRegestration semesterRegistration = new SemesterRegestration();
        semesterRegistration.setAcademicRegistration(registration);
        semesterRegistration.setSemester(semester);
        semesterRegistration = semesterRegistrationRepository.save(semesterRegistration);

        classAssignmentService.assignStudentClass(
            root,
            registration.getId(),
            semester.getId(),
            new AssignStudentClassRequest(classGroup.getId())
        );

        SubjectModuleRegestration moduleRegistration = new SubjectModuleRegestration();
        moduleRegistration.setSemesterRegestration(semesterRegistration);
        moduleRegistration.setSubjectModule(subjectModule);
        moduleRegistration.setOriginAcademicLevel(null);
        moduleRegistration.setInscriptionNumber(1);
        moduleRegistration.setStatus(SubjectModuleRegistrationStatus.ACTIVE);
        return moduleRegistrationRepository.save(moduleRegistration);
    }

    private Student saveStudent(String email) {
        UserAccount account = saveAccount(email, AccountRoleType.STUDENT);
        Student student = new Student();
        student.setUserAccount(account);
        student.setEstablishment(establishment);
        return studentRepository.save(student);
    }

    private Professor saveProfessor() {
        return saveProfessor("professor@uiz.ac.ma");
    }

    private Professor saveProfessor(String email) {
        UserAccount account = saveAccount(email, AccountRoleType.PROFESSOR);
        Professor savedProfessor = new Professor();
        savedProfessor.setUserAccount(account);
        savedProfessor.setEstablishment(establishment);
        return professorRepository.save(savedProfessor);
    }

    private Admin saveAdmin() {
        UserAccount account = saveAccount("admin@uiz.ac.ma", AccountRoleType.ADMIN);
        Admin admin = new Admin();
        admin.setUserAccount(account);
        admin.setEstablishment(establishment);
        return adminRepository.save(admin);
    }

    private UserAccount saveAccount(String email, AccountRoleType role) {
        UserAccount account = new UserAccount();
        account.setUniversityEmail(email);
        account.setPasswordHash("not-used-by-this-test");
        account.setRole(role);
        account.setAccountStatus(AccountStatus.ACTIVE);
        return userAccountRepository.save(account);
    }

    private void grant(Admin admin, PermissionCode code) {
        Permission permission = permissionRepository.findByCodeIn(Set.of(code)).get(0);
        AdminPermissionGrant grant = new AdminPermissionGrant();
        grant.setAdmin(admin);
        grant.setPermission(permission);
        adminPermissionGrantRepository.save(grant);
    }

    private TeachingAssignment saveTeachingAssignment() {
        TeachingAssignment assignment = new TeachingAssignment();
        assignment.setProfessor(professor);
        assignment.setSubjectModule(subjectModule);
        assignment.setClassGroup(classGroup);
        assignment.setAcademicYear(academicYear);
        assignment.setSemester(semester);
        assignment.setStatus(TeachingAssignmentStatus.ACTIVE);
        return teachingAssignmentRepository.save(assignment);
    }

    private SubjectModule saveSubjectModule(Semester savedSemester) {
        SubjectModule module = new SubjectModule();
        module.setSemester(savedSemester);
        module.setCode("ALG");
        module.setTitle("Algorithms");
        return subjectModuleRepository.save(module);
    }

    private ClassGroup saveClassGroup(AcademicLevel level, AcademicYear year) {
        ClassGroup group = new ClassGroup();
        group.setAcademicLevel(level);
        group.setAcademicYear(year);
        group.setName("Group A");
        group.setStatus(ClassGroupStatus.ACTIVE);
        return classGroupRepository.save(group);
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

    private Establishment saveEstablishment(University university) {
        Establishment saved = new Establishment();
        saved.setUniversity(university);
        saved.setName("ENSA Agadir");
        saved.setEstablishmentType(EstablishmentType.SCHOOL);
        saved.setEstablishmentStatus(EstablishmentStatus.ACTIVE);
        return establishmentRepository.save(saved);
    }

    private ProgramFiliere saveProgram(Establishment savedEstablishment) {
        Department department = new Department();
        department.setEstablishment(savedEstablishment);
        department.setName("Computer Science");
        department = departmentRepository.save(department);

        DegreeCycle cycle = new DegreeCycle();
        cycle.setEstablishment(savedEstablishment);
        cycle.setName("Engineering");
        cycle = degreeCycleRepository.save(cycle);

        ProgramPath path = new ProgramPath();
        path.setEstablishment(savedEstablishment);
        path.setName("Normal");
        path = programPathRepository.save(path);

        ProgramFiliere savedProgram = new ProgramFiliere();
        savedProgram.setDepartment(department);
        savedProgram.setDegreeCycle(cycle);
        savedProgram.setProgramPath(path);
        savedProgram.setCode("IL");
        savedProgram.setName("Software Engineering");
        return programFiliereRepository.save(savedProgram);
    }

    private AcademicLevel saveLevel(ProgramFiliere savedProgram) {
        AcademicLevel level = new AcademicLevel();
        level.setProgramFiliere(savedProgram);
        level.setName("M1");
        level.setLevelOrder(1);
        return academicLevelRepository.save(level);
    }

    private AcademicYear saveYear(Establishment savedEstablishment) {
        AcademicYear year = new AcademicYear();
        year.setEstablishment(savedEstablishment);
        year.setLabel("2026-2027");
        year.setStartYear(2026);
        year.setEndYear(2027);
        year.setStatus(AcademicYearStatus.ACTIVE);
        return academicYearRepository.save(year);
    }

    private Semester saveSemester(AcademicLevel level, AcademicYear year) {
        Semester savedSemester = new Semester();
        savedSemester.setAcademicLevel(level);
        savedSemester.setAcademicYear(year);
        savedSemester.setName("S1");
        savedSemester.setSemesterOrder(1);
        return semesterRepository.save(savedSemester);
    }

    private void clearBusinessData() {
        gradeRecordRepository.deleteAll();
        moduleExamRepository.deleteAll();
        examScheduleRepository.deleteAll();
        teachingAssignmentRepository.deleteAll();
        classAssignmentRepository.deleteAll();
        adminPermissionGrantRepository.deleteAll();
        moduleRegistrationRepository.deleteAll();
        semesterRegistrationRepository.deleteAll();
        academicRegistrationRepository.deleteAll();
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
