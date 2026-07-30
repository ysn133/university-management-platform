package com.platform.academicregistration.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.platform.academicregistration.registration.application.AcademicRegistrationService;
import com.platform.academicregistration.registration.domain.AcademicRegistrationStatus;
import com.platform.academicregistration.registration.infrastructure.AcademicRegistrationRepository;
import com.platform.academicregistration.registration.presentation.dto.AcademicRegistrationResponse;
import com.platform.academicregistration.registration.presentation.dto.CreateAcademicRegistrationRequest;
import com.platform.academicregistration.registration.presentation.dto.UpdateAcademicRegistrationRequest;
import com.platform.academicregistration.semesterregistration.infrastructer.SemesterRegestrationRepository;
import com.platform.academicregistration.subjectmoduleregestration.infrastructure.SubjectRegestrationRepository;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.Student;
import com.platform.identityaccess.domain.UserAccount;
import com.platform.identityaccess.infrastructure.StudentRepository;
import com.platform.identityaccess.infrastructure.UserAccountRepository;
import com.platform.platform.PlatformApplication;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academiclevel.infrastructure.AcademicLevelRepository;
import com.platform.universitygovernance.academiclevelruleassignment.domain.AcademicLevelRuleAssignment;
import com.platform.universitygovernance.academiclevelruleassignment.domain.AcademicLevelRuleAssignmentStatus;
import com.platform.universitygovernance.academiclevelruleassignment.infrastructure.AcademicLevelRuleAssignmentRepository;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicyear.domain.AcademicYearStatus;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfile;
import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfileStatus;
import com.platform.universitygovernance.academicruleprofile.domain.SessionGradePolicy;
import com.platform.universitygovernance.academicruleprofile.infrastructure.AcademicRuleProfileRepository;
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
class AcademicRegistrationServiceIntegrationTest {

    @Autowired
    private AcademicRegistrationService academicRegistrationService;

    @Autowired
    private AcademicRegistrationRepository academicRegistrationRepository;

    @Autowired
    private SemesterRegestrationRepository semesterRegestrationRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private SubjectRegestrationRepository subjectRegestrationRepository;

    @Autowired
    private SubjectModuleRepository subjectModuleRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private AcademicLevelRepository academicLevelRepository;

    @Autowired
    private AcademicLevelRuleAssignmentRepository ruleAssignmentRepository;

    @Autowired
    private AcademicRuleProfileRepository academicRuleProfileRepository;

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
    private Student student;
    private ProgramFiliere firstProgram;
    private AcademicLevel firstLevel;
    private AcademicLevel secondLevel;
    private AcademicYear firstYear;
    private AcademicYear secondYear;
    private AcademicRuleProfile firstRuleProfile;

    @BeforeEach
    void setUp() {
        clearAcademicData();

        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);

        firstEstablishment = saveEstablishment(university, "ENSA Agadir");
        secondEstablishment = saveEstablishment(university, "Faculty of Sciences");
        firstProgram = saveProgram(firstEstablishment, "IL");
        ProgramFiliere secondProgram = saveProgram(secondEstablishment, "MATH");
        firstLevel = saveLevel(firstProgram, "M1", 1);
        secondLevel = saveLevel(secondProgram, "L1", 1);
        firstYear = saveYear(firstEstablishment, "2026-2027", 2026);
        secondYear = saveYear(firstEstablishment, "2027-2028", 2027);
        firstRuleProfile = saveRuleProfile(firstEstablishment, "Standard Master Rules");
        saveRuleAssignment(firstLevel, firstYear, firstRuleProfile);
        saveRuleAssignment(firstLevel, secondYear, firstRuleProfile);
        Semester firstYearS1 = saveSemester(firstLevel, firstYear, "S1", 1);
        Semester firstYearS2 = saveSemester(firstLevel, firstYear, "S2", 2);
        Semester secondYearS1 = saveSemester(firstLevel, secondYear, "S1", 1);
        Semester secondYearS2 = saveSemester(firstLevel, secondYear, "S2", 2);
        saveSubjectModule(firstYearS1, "ALG", "Algorithms");
        saveSubjectModule(firstYearS2, "DB", "Databases");
        saveSubjectModule(secondYearS1, "AI", "Artificial Intelligence");
        saveSubjectModule(secondYearS2, "SEC", "Security");
        student = saveStudent(firstEstablishment, "student@ensa.uiz.ac.ma");
    }

    @AfterEach
    void tearDown() {
        clearAcademicData();
    }

    @Test
    void rootCanCreateListReadAndUpdateAnnualRegistrations() {
        AuthenticatedUserPrincipal root = rootPrincipal();

        AcademicRegistrationResponse first = academicRegistrationService
            .createAcademicRegistration(
                root,
                firstEstablishment.getId(),
                request(firstYear, firstProgram, firstLevel)
            );

        assertThat(first.status()).isEqualTo(AcademicRegistrationStatus.ACTIVE);
        assertThat(first.studentId()).isEqualTo(student.getId());
        assertThat(semesterRegestrationRepository.findAll())
            .filteredOn(registration -> registration
                .getAcademicRegistration()
                .getId()
                .equals(first.id()))
            .hasSize(2);
        assertThat(subjectRegestrationRepository.findAll())
            .filteredOn(registration -> registration
                .getSemesterRegestration()
                .getAcademicRegistration()
                .getId()
                .equals(first.id()))
            .hasSize(2)
            .allSatisfy(registration -> {
                assertThat(registration.getInscriptionNumber()).isEqualTo(1);
                assertThat(registration.getOriginAcademicLevel()).isNull();
            });
        assertThat(academicRegistrationService.getAcademicRegistration(root, first.id()).id())
            .isEqualTo(first.id());

        assertThatThrownBy(() -> academicRegistrationService.createAcademicRegistration(
            root,
            firstEstablishment.getId(),
            request(firstYear, firstProgram, firstLevel)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");

        AcademicRegistrationResponse second = academicRegistrationService
            .createAcademicRegistration(
                root,
                firstEstablishment.getId(),
                request(secondYear, firstProgram, firstLevel)
            );

        assertThat(academicRegistrationService.getAcademicRegistrations(
            root,
            firstEstablishment.getId()
        ))
            .extracting(AcademicRegistrationResponse::id)
            .containsExactly(second.id(), first.id());
        assertThat(academicRegistrationService.getStudentAcademicRegistrations(
            root,
            student.getId()
        ))
            .extracting(AcademicRegistrationResponse::id)
            .containsExactly(second.id(), first.id());

        AcademicRegistrationResponse suspended = academicRegistrationService
            .updateAcademicRegistration(
                root,
                first.id(),
                new UpdateAcademicRegistrationRequest(AcademicRegistrationStatus.SUSPENDED)
            );
        assertThat(suspended.status()).isEqualTo(AcademicRegistrationStatus.SUSPENDED);
    }

    @Test
    void registrationRollsBackWhenTwoSemestersAreNotConfigured() {
        AuthenticatedUserPrincipal root = rootPrincipal();
        AcademicYear yearWithoutSemesters = saveYear(
            firstEstablishment,
            "2028-2029",
            2028
        );
        saveRuleAssignment(firstLevel, yearWithoutSemesters, firstRuleProfile);

        assertThatThrownBy(() -> academicRegistrationService.createAcademicRegistration(
            root,
            firstEstablishment.getId(),
            request(yearWithoutSemesters, firstProgram, firstLevel)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST")
            .hasMessageContaining("Exactly two semesters");

        assertThat(academicRegistrationRepository.existsByStudentIdAndAcademicYearId(
            student.getId(),
            yearWithoutSemesters.getId()
        )).isFalse();
    }

    @Test
    void registrationContextMustBelongToOneEstablishmentAndProgram() {
        AuthenticatedUserPrincipal root = rootPrincipal();

        assertThatThrownBy(() -> academicRegistrationService.createAcademicRegistration(
            root,
            firstEstablishment.getId(),
            request(firstYear, firstProgram, secondLevel)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");
    }

    @Test
    void registrationRequiresAnActiveRuleAssignment() {
        AuthenticatedUserPrincipal root = rootPrincipal();
        AcademicYear yearWithoutAssignment = saveYear(
            firstEstablishment,
            "2029-2030",
            2029
        );

        assertThatThrownBy(() -> academicRegistrationService.createAcademicRegistration(
            root,
            firstEstablishment.getId(),
            request(yearWithoutAssignment, firstProgram, firstLevel)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST")
            .hasMessageContaining("active rule assignment");
    }

    private CreateAcademicRegistrationRequest request(
        AcademicYear academicYear,
        ProgramFiliere programFiliere,
        AcademicLevel academicLevel
    ) {
        return new CreateAcademicRegistrationRequest(
            student.getId(),
            programFiliere.getId(),
            academicLevel.getId(),
            academicYear.getId()
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

    private AcademicLevel saveLevel(ProgramFiliere program, String name, int order) {
        AcademicLevel level = new AcademicLevel();
        level.setProgramFiliere(program);
        level.setName(name);
        level.setLevelOrder(order);
        return academicLevelRepository.save(level);
    }

    private AcademicYear saveYear(Establishment establishment, String label, int startYear) {
        AcademicYear year = new AcademicYear();
        year.setEstablishment(establishment);
        year.setLabel(label);
        year.setStartYear(startYear);
        year.setEndYear(startYear + 1);
        year.setStatus(AcademicYearStatus.ACTIVE);
        return academicYearRepository.save(year);
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

    private AcademicLevelRuleAssignment saveRuleAssignment(
        AcademicLevel academicLevel,
        AcademicYear academicYear,
        AcademicRuleProfile profile
    ) {
        AcademicLevelRuleAssignment assignment = new AcademicLevelRuleAssignment();
        assignment.setAcademicLevel(academicLevel);
        assignment.setAcademicYear(academicYear);
        assignment.setAcademicRuleProfile(profile);
        assignment.setStatus(AcademicLevelRuleAssignmentStatus.ACTIVE);
        return ruleAssignmentRepository.save(assignment);
    }

    private Semester saveSemester(
        AcademicLevel academicLevel,
        AcademicYear academicYear,
        String name,
        int semesterOrder
    ) {
        Semester semester = new Semester();
        semester.setAcademicLevel(academicLevel);
        semester.setAcademicYear(academicYear);
        semester.setName(name);
        semester.setSemesterOrder(semesterOrder);
        return semesterRepository.save(semester);
    }

    private SubjectModule saveSubjectModule(Semester semester, String code, String title) {
        SubjectModule subjectModule = new SubjectModule();
        subjectModule.setSemester(semester);
        subjectModule.setCode(code);
        subjectModule.setTitle(title);
        return subjectModuleRepository.save(subjectModule);
    }

    private Student saveStudent(Establishment establishment, String email) {
        UserAccount account = new UserAccount();
        account.setUniversityEmail(email);
        account.setPasswordHash("not-used-by-this-test");
        account.setRole(AccountRoleType.STUDENT);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account = userAccountRepository.save(account);

        Student savedStudent = new Student();
        savedStudent.setUserAccount(account);
        savedStudent.setEstablishment(establishment);
        savedStudent.setApogeeCode("APO-" + UUID.randomUUID());
        return studentRepository.save(savedStudent);
    }

    private AuthenticatedUserPrincipal rootPrincipal() {
        return new AuthenticatedUserPrincipal(
            UUID.randomUUID(),
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null,
            null
        );
    }

    private void clearAcademicData() {
        var studentAccounts = studentRepository.findAll().stream()
            .map(Student::getUserAccount)
            .toList();
        subjectRegestrationRepository.deleteAll();
        semesterRegestrationRepository.deleteAll();
        academicRegistrationRepository.deleteAll();
        ruleAssignmentRepository.deleteAll();
        studentRepository.deleteAll();
        userAccountRepository.deleteAll(studentAccounts);
        subjectModuleRepository.deleteAll();
        semesterRepository.deleteAll();
        academicLevelRepository.deleteAll();
        academicYearRepository.deleteAll();
        academicRuleProfileRepository.deleteAll();
        programFiliereRepository.deleteAll();
        degreeCycleRepository.deleteAll();
        programPathRepository.deleteAll();
        departmentRepository.deleteAll();
        establishmentRepository.deleteAll();
        universityRepository.deleteAll();
    }
}
