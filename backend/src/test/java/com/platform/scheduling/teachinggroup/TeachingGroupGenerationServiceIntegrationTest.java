package com.platform.scheduling.teachinggroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.platform.academicregistration.classassignment.domain.StudentClassAssignment;
import com.platform.academicregistration.classassignment.infrastructure.StudentClassAssignmentRepository;
import com.platform.academicregistration.registration.domain.AcademicRegistration;
import com.platform.academicregistration.registration.domain.AcademicRegistrationStatus;
import com.platform.academicregistration.registration.infrastructure.AcademicRegistrationRepository;
import com.platform.academicregistration.semesterregistration.domain.SemesterRegistration;
import com.platform.academicregistration.semesterregistration.infrastructure.SemesterRegistrationRepository;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.Student;
import com.platform.identityaccess.domain.UserAccount;
import com.platform.identityaccess.infrastructure.StudentRepository;
import com.platform.identityaccess.infrastructure.UserAccountRepository;
import com.platform.platform.PlatformApplication;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.domain.RoomType;
import com.platform.scheduling.teachinggroup.application.TeachingGroupGenerationService;
import com.platform.scheduling.teachinggroup.domain.TeachingGroup;
import com.platform.scheduling.teachinggroup.domain.TeachingGroupMembership;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupMembershipRepository;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupRepository;
import com.platform.teachingrequirement.application.TeachingRequirementService;
import com.platform.teachingrequirement.infrastructure.TeachingRequirementRepository;
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
import com.platform.universitygovernance.moduleteachingcomponent.domain.ModuleTeachingComponent;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import com.platform.universitygovernance.moduleteachingcomponent.infrastructure.ModuleTeachingComponentRepository;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(classes = PlatformApplication.class)
@ActiveProfiles("test")
class TeachingGroupGenerationServiceIntegrationTest {

    @Autowired
    private TeachingGroupGenerationService generationService;

    @Autowired
    private TeachingRequirementService teachingRequirementService;

    @Autowired
    private TeachingRequirementRepository teachingRequirementRepository;

    @Autowired
    private TeachingGroupMembershipRepository membershipRepository;

    @Autowired
    private TeachingGroupRepository teachingGroupRepository;

    @Autowired
    private ModuleTeachingComponentRepository componentRepository;

    @Autowired
    private StudentClassAssignmentRepository classAssignmentRepository;

    @Autowired
    private SemesterRegistrationRepository semesterRegistrationRepository;

    @Autowired
    private AcademicRegistrationRepository academicRegistrationRepository;

    @Autowired
    private StudentRepository studentRepository;

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
    private ProgramFiliere program;
    private AcademicLevel academicLevel;
    private AcademicYear academicYear;
    private Semester semester;
    private ClassGroup groupA;
    private ClassGroup groupB;

    @BeforeEach
    void setUp() {
        clearBusinessData();
        createAcademicStructure();
    }

    @AfterEach
    void tearDown() {
        clearBusinessData();
    }

    @Test
    void generatesCohortClassAndBalancedSubgroupAudiences() {
        for (int index = 1; index <= 5; index++) {
            saveActiveRegistration(index, groupA);
        }
        for (int index = 6; index <= 10; index++) {
            saveActiveRegistration(index, groupB);
        }

        List<TeachingGroup> generated = generationService.generateForSemester(semester.getId());
        assertThat(generated).hasSize(7);

        Map<String, TeachingGroup> groupsByName = generated.stream()
            .collect(Collectors.toMap(TeachingGroup::getName, Function.identity()));
        assertThat(groupsByName.keySet())
            .containsExactlyInAnyOrder("Whole Cohort", "A", "B", "A1", "A2", "B1", "B2");
        assertThat(groupsByName.get("Whole Cohort").getSourceClassGroup()).isNull();
        assertThat(groupsByName.get("A1").getSourceClassGroup().getId()).isEqualTo(groupA.getId());
        assertThat(groupsByName.get("B2").getSourceClassGroup().getId()).isEqualTo(groupB.getId());

        Map<String, Long> membershipCounts = membershipRepository
            .findByTeachingGroupIdIn(generated.stream().map(TeachingGroup::getId).toList())
            .stream()
            .collect(Collectors.groupingBy(
                membership -> membership.getTeachingGroup().getName(),
                Collectors.counting()
            ));
        assertThat(membershipCounts).containsEntry("Whole Cohort", 10L);
        assertThat(membershipCounts).containsEntry("A", 5L).containsEntry("B", 5L);
        assertThat(membershipCounts).containsEntry("A1", 3L).containsEntry("A2", 2L);
        assertThat(membershipCounts).containsEntry("B1", 3L).containsEntry("B2", 2L);

        for (TeachingGroupMembership membership : membershipRepository.findAll()) {
            if (membership.getTeachingGroup().getAudienceType() == TeachingAudienceMode.SUBGROUP) {
                ClassGroup assignedClass = classAssignmentRepository
                    .findBySemesterRegistrationId(membership.getSemesterRegistration().getId())
                    .orElseThrow()
                    .getClassGroup();
                assertThat(assignedClass.getId())
                    .isEqualTo(membership.getTeachingGroup().getSourceClassGroup().getId());
            }
        }
    }

    @Test
    void rejectsGenerationUntilEveryActiveStudentHasAClassAssignment() {
        saveActiveRegistration(1, groupA);
        saveActiveRegistration(2, null);

        assertThatThrownBy(() -> generationService.generateForSemester(semester.getId()))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");
        assertThat(teachingGroupRepository.findAll()).isEmpty();
        assertThat(membershipRepository.findAll()).isEmpty();
    }

    @Test
    void generatesOneRequirementForEachMatchingTeachingGroup() {
        for (int index = 1; index <= 5; index++) {
            saveActiveRegistration(index, groupA);
        }
        for (int index = 6; index <= 10; index++) {
            saveActiveRegistration(index, groupB);
        }
        generationService.generateForSemester(semester.getId());

        AuthenticatedUserPrincipal root = new AuthenticatedUserPrincipal(
            UUID.randomUUID(),
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null,
            "root@uiz.ac.ma"
        );
        var generated = teachingRequirementService.generateForSemester(
            root,
            semester.getId()
        );

        assertThat(generated).hasSize(4);
        assertThat(generated)
            .allMatch(requirement -> requirement.componentType()
                == TeachingComponentType.TP)
            .allMatch(requirement -> requirement.audienceType()
                == TeachingAudienceMode.SUBGROUP);
        assertThat(teachingRequirementService.generateForSemester(
            root,
            semester.getId()
        )).hasSize(4);
        assertThatThrownBy(() -> generationService.generateForSemester(semester.getId()))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");
    }

    private void createAcademicStructure() {
        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);

        establishment = new Establishment();
        establishment.setUniversity(university);
        establishment.setName("ENSA Agadir");
        establishment.setEstablishmentType(EstablishmentType.SCHOOL);
        establishment.setEstablishmentStatus(EstablishmentStatus.ACTIVE);
        establishment = establishmentRepository.save(establishment);

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
        programPath.setName("Excellence");
        programPath = programPathRepository.save(programPath);

        program = new ProgramFiliere();
        program.setDepartment(department);
        program.setDegreeCycle(degreeCycle);
        program.setProgramPath(programPath);
        program.setCode("IL");
        program.setName("Software Engineering");
        program = programFiliereRepository.save(program);

        academicLevel = new AcademicLevel();
        academicLevel.setProgramFiliere(program);
        academicLevel.setName("M1");
        academicLevel.setLevelOrder(1);
        academicLevel = academicLevelRepository.save(academicLevel);

        academicYear = new AcademicYear();
        academicYear.setEstablishment(establishment);
        academicYear.setLabel("2026-2027");
        academicYear.setStartYear(2026);
        academicYear.setEndYear(2027);
        academicYear.setStatus(AcademicYearStatus.ACTIVE);
        academicYear = academicYearRepository.save(academicYear);

        semester = new Semester();
        semester.setAcademicLevel(academicLevel);
        semester.setAcademicYear(academicYear);
        semester.setName("S1");
        semester.setSemesterOrder(1);
        semester = semesterRepository.save(semester);

        groupA = saveClassGroup("A");
        groupB = saveClassGroup("B");

        SubjectModule subjectModule = new SubjectModule();
        subjectModule.setSemester(semester);
        subjectModule.setCode("ALG101");
        subjectModule.setTitle("Algorithms");
        subjectModule = subjectModuleRepository.save(subjectModule);

        ModuleTeachingComponent component = new ModuleTeachingComponent();
        component.setSubjectModule(subjectModule);
        component.setComponentType(TeachingComponentType.TP);
        component.setSessionsPerWeek(1);
        component.setSessionDurationMinutes(120);
        component.setAudienceMode(TeachingAudienceMode.SUBGROUP);
        component.setMaximumGroupSize(3);
        component.setRequiredRoomType(RoomType.COMPUTER_LAB);
        componentRepository.save(component);
    }

    private ClassGroup saveClassGroup(String name) {
        ClassGroup classGroup = new ClassGroup();
        classGroup.setAcademicLevel(academicLevel);
        classGroup.setAcademicYear(academicYear);
        classGroup.setName(name);
        classGroup.setStatus(ClassGroupStatus.ACTIVE);
        return classGroupRepository.save(classGroup);
    }

    private void saveActiveRegistration(int index, ClassGroup classGroup) {
        UserAccount account = new UserAccount();
        account.setUniversityEmail("student" + index + "@uiz.ac.ma");
        account.setPasswordHash("not-used-by-this-test");
        account.setRole(AccountRoleType.STUDENT);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account = userAccountRepository.save(account);

        Student student = new Student();
        student.setUserAccount(account);
        student.setEstablishment(establishment);
        student.setApogeeCode("APO-" + String.format("%04d", index));
        student = studentRepository.save(student);

        AcademicRegistration academicRegistration = new AcademicRegistration();
        academicRegistration.setStudent(student);
        academicRegistration.setProgramFiliere(program);
        academicRegistration.setAcademicLevel(academicLevel);
        academicRegistration.setAcademicYear(academicYear);
        academicRegistration.setStatus(AcademicRegistrationStatus.ACTIVE);
        academicRegistration = academicRegistrationRepository.save(academicRegistration);

        SemesterRegistration semesterRegistration = new SemesterRegistration();
        semesterRegistration.setAcademicRegistration(academicRegistration);
        semesterRegistration.setSemester(semester);
        semesterRegistration = semesterRegistrationRepository.save(semesterRegistration);

        if (classGroup != null) {
            StudentClassAssignment assignment = new StudentClassAssignment();
            assignment.setSemesterRegistration(semesterRegistration);
            assignment.setClassGroup(classGroup);
            classAssignmentRepository.save(assignment);
        }
    }

    private void clearBusinessData() {
        teachingRequirementRepository.deleteAll();
        membershipRepository.deleteAll();
        teachingGroupRepository.deleteAll();
        componentRepository.deleteAll();
        classAssignmentRepository.deleteAll();
        semesterRegistrationRepository.deleteAll();
        academicRegistrationRepository.deleteAll();
        studentRepository.deleteAll();
        subjectModuleRepository.deleteAll();
        classGroupRepository.deleteAll();
        semesterRepository.deleteAll();
        academicLevelRepository.deleteAll();
        academicYearRepository.deleteAll();
        programFiliereRepository.deleteAll();
        degreeCycleRepository.deleteAll();
        programPathRepository.deleteAll();
        departmentRepository.deleteAll();
        establishmentRepository.deleteAll();
        universityRepository.deleteAll();
        userAccountRepository.deleteAll();
    }
}
