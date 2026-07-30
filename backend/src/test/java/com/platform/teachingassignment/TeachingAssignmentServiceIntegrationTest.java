package com.platform.teachingassignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.Professor;
import com.platform.identityaccess.domain.UserAccount;
import com.platform.identityaccess.infrastructure.AdminPermissionGrantRepository;
import com.platform.identityaccess.infrastructure.AdminRepository;
import com.platform.identityaccess.infrastructure.ProfessorRepository;
import com.platform.identityaccess.infrastructure.RootSuperAdminRepository;
import com.platform.identityaccess.infrastructure.StudentRepository;
import com.platform.identityaccess.infrastructure.SuperAdminRepository;
import com.platform.identityaccess.infrastructure.UserAccountRepository;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.PlatformApplication;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.teachingassignment.application.TeachingAssignmentService;
import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import com.platform.teachingassignment.infrastructure.TeachingAssignmentRepository;
import com.platform.teachingassignment.presentation.dto.CreateTeachingAssignmentRequest;
import com.platform.teachingassignment.presentation.dto.TeachingAssignmentResponse;
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
class TeachingAssignmentServiceIntegrationTest {

    @Autowired
    private TeachingAssignmentService teachingAssignmentService;

    @Autowired
    private TeachingAssignmentRepository teachingAssignmentRepository;

    @Autowired
    private ProfessorRepository professorRepository;

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

    @Autowired
    private AdminPermissionGrantRepository adminPermissionGrantRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private RootSuperAdminRepository rootSuperAdminRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private Establishment establishment;
    private AcademicYear academicYear;
    private Semester semester;
    private SubjectModule subjectModule;
    private ClassGroup classGroup;

    @BeforeEach
    void setUp() {
        clearBusinessData();

        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);

        establishment = new Establishment();
        establishment.setUniversity(university);
        establishment.setName("ENSA Agadir");
        establishment.setEstablishmentType(EstablishmentType.SCHOOL);
        establishment.setEstablishmentStatus(EstablishmentStatus.ACTIVE);
        establishment = establishmentRepository.save(establishment);

        ProgramFiliere program = saveProgram();
        AcademicLevel level = new AcademicLevel();
        level.setProgramFiliere(program);
        level.setName("M1");
        level.setLevelOrder(1);
        level = academicLevelRepository.save(level);

        academicYear = new AcademicYear();
        academicYear.setEstablishment(establishment);
        academicYear.setLabel("2026-2027");
        academicYear.setStartYear(2026);
        academicYear.setEndYear(2027);
        academicYear.setStatus(AcademicYearStatus.ACTIVE);
        academicYear = academicYearRepository.save(academicYear);

        semester = new Semester();
        semester.setAcademicLevel(level);
        semester.setAcademicYear(academicYear);
        semester.setName("S1");
        semester.setSemesterOrder(1);
        semester = semesterRepository.save(semester);

        subjectModule = new SubjectModule();
        subjectModule.setSemester(semester);
        subjectModule.setCode("ALG");
        subjectModule.setTitle("Algorithms");
        subjectModule = subjectModuleRepository.save(subjectModule);

        classGroup = new ClassGroup();
        classGroup.setAcademicLevel(level);
        classGroup.setAcademicYear(academicYear);
        classGroup.setName("Group A");
        classGroup.setStatus(ClassGroupStatus.ACTIVE);
        classGroup = classGroupRepository.save(classGroup);
    }

    @AfterEach
    void tearDown() {
        clearBusinessData();
    }

    @Test
    void teachingScopeAllowsOnlyOneActiveProfessor() {
        AuthenticatedUserPrincipal root = principal(
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null
        );
        Professor firstProfessor = saveProfessor("first.professor@uiz.ac.ma");
        Professor secondProfessor = saveProfessor("second.professor@uiz.ac.ma");

        TeachingAssignmentResponse first = teachingAssignmentService
            .createTeachingAssignment(
                root,
                establishment.getId(),
                request(firstProfessor)
            );

        assertThat(first.status()).isEqualTo(TeachingAssignmentStatus.ACTIVE);
        assertThat(teachingAssignmentService.getTeachingAssignment(root, first.id()).id())
            .isEqualTo(first.id());
        assertThat(teachingAssignmentService.getTeachingAssignments(
            root,
            establishment.getId()
        ))
            .extracting(TeachingAssignmentResponse::id)
            .containsExactly(first.id());

        AuthenticatedUserPrincipal professorPrincipal = principal(
            AccountRoleType.PROFESSOR,
            firstProfessor.getId(),
            establishment.getId()
        );
        assertThat(teachingAssignmentService.getMyTeachingAssignments(
            professorPrincipal
        ))
            .extracting(TeachingAssignmentResponse::id)
            .containsExactly(first.id());

        assertThatThrownBy(() -> teachingAssignmentService.createTeachingAssignment(
            root,
            establishment.getId(),
            request(secondProfessor)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");

        ActionResponse unassigned = teachingAssignmentService.unassignProfessor(
            root,
            first.id()
        );
        assertThat(unassigned.success()).isTrue();
        assertThat(teachingAssignmentRepository.findById(first.id()).orElseThrow()
            .getStatus()).isEqualTo(TeachingAssignmentStatus.INACTIVE);

        TeachingAssignmentResponse replacement = teachingAssignmentService
            .createTeachingAssignment(
                root,
                establishment.getId(),
                request(secondProfessor)
            );
        assertThat(replacement.professorId()).isEqualTo(secondProfessor.getId());
        assertThat(replacement.status()).isEqualTo(TeachingAssignmentStatus.ACTIVE);
    }

    private CreateTeachingAssignmentRequest request(Professor professor) {
        return new CreateTeachingAssignmentRequest(
            professor.getId(),
            subjectModule.getId(),
            classGroup.getId(),
            academicYear.getId(),
            semester.getId()
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

    private ProgramFiliere saveProgram() {
        Department department = new Department();
        department.setEstablishment(establishment);
        department.setName("Computer Science");
        department = departmentRepository.save(department);

        DegreeCycle degreeCycle = new DegreeCycle();
        degreeCycle.setEstablishment(establishment);
        degreeCycle.setName("Master");
        degreeCycle = degreeCycleRepository.save(degreeCycle);

        ProgramPath programPath = new ProgramPath();
        programPath.setEstablishment(establishment);
        programPath.setName("Normal");
        programPath = programPathRepository.save(programPath);

        ProgramFiliere program = new ProgramFiliere();
        program.setDepartment(department);
        program.setDegreeCycle(degreeCycle);
        program.setProgramPath(programPath);
        program.setCode("IL");
        program.setName("Software Engineering");
        return programFiliereRepository.save(program);
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

    private void clearBusinessData() {
        teachingAssignmentRepository.deleteAll();
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
