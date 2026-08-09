package com.platform.universitygovernance.classgroup;

import static org.assertj.core.api.Assertions.assertThat;

import com.platform.academicregistration.classassignment.application.StudentClassAssignmentService;
import com.platform.academicregistration.classassignment.domain.StudentClassAssignment;
import com.platform.academicregistration.classassignment.infrastructure.StudentClassAssignmentRepository;
import com.platform.academicregistration.classassignment.presentation.dto.BulkAssignStudentClassesRequest;
import com.platform.academicregistration.classassignment.presentation.dto.BulkClassAssignmentItemRequest;
import com.platform.academicregistration.classassignment.presentation.dto.BulkClassAssignmentResponse;
import com.platform.academicregistration.classassignment.presentation.dto.ClassGroupRosterResponse;
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
import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academiclevel.infrastructure.AcademicLevelRepository;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicyear.domain.AcademicYearStatus;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.classgroup.application.ClassGroupGenerationService;
import com.platform.universitygovernance.classgroup.application.ClassGroupService;
import com.platform.universitygovernance.classgroup.domain.ClassGroup;
import com.platform.universitygovernance.classgroup.infrastructure.ClassGroupRepository;
import com.platform.universitygovernance.classgroup.domain.ClassGroupStatus;
import com.platform.universitygovernance.classgroup.presentation.dto.ClassGroupGenerationResponse;
import com.platform.universitygovernance.classgroup.presentation.dto.ClassGroupResponse;
import com.platform.universitygovernance.classgroup.presentation.dto.ClassGroupRebalanceResponse;
import com.platform.universitygovernance.classgroup.presentation.dto.CreateClassGroupRequest;
import com.platform.universitygovernance.classgroup.presentation.dto.GenerateClassGroupsRequest;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = PlatformApplication.class)
@ActiveProfiles("test")
@Transactional
class ClassGroupDistributionIntegrationTest {

    @Autowired
    private ClassGroupGenerationService generationService;

    @Autowired
    private ClassGroupService classGroupService;

    @Autowired
    private StudentClassAssignmentService classAssignmentService;

    @Autowired
    private StudentClassAssignmentRepository classAssignmentRepository;

    @Autowired
    private ClassGroupRepository classGroupRepository;

    @Autowired
    private AcademicRegistrationRepository academicRegistrationRepository;

    @Autowired
    private SemesterRegistrationRepository semesterRegistrationRepository;

    @Autowired
    private StudentRepository studentRepository;

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

    private AcademicLevel academicLevel;
    private AcademicYear academicYear;
    private List<AcademicRegistration> registrations;
    private AuthenticatedUserPrincipal root;

    @BeforeEach
    void setUp() {
        University university = new University();
        university.setName("Distribution Test University " + UUID.randomUUID());
        university = universityRepository.save(university);

        Establishment establishment = new Establishment();
        establishment.setUniversity(university);
        establishment.setName("Distribution Test School " + UUID.randomUUID());
        establishment.setEstablishmentType(EstablishmentType.SCHOOL);
        establishment.setEstablishmentStatus(EstablishmentStatus.ACTIVE);
        establishment = establishmentRepository.save(establishment);

        Department department = new Department();
        department.setEstablishment(establishment);
        department.setName("Computer Science " + UUID.randomUUID());
        department = departmentRepository.save(department);

        DegreeCycle degreeCycle = new DegreeCycle();
        degreeCycle.setEstablishment(establishment);
        degreeCycle.setName("Master " + UUID.randomUUID());
        degreeCycle = degreeCycleRepository.save(degreeCycle);

        ProgramPath programPath = new ProgramPath();
        programPath.setEstablishment(establishment);
        programPath.setName("Regular " + UUID.randomUUID());
        programPath = programPathRepository.save(programPath);

        ProgramFiliere program = new ProgramFiliere();
        program.setDepartment(department);
        program.setDegreeCycle(degreeCycle);
        program.setProgramPath(programPath);
        program.setCode("DIST-" + UUID.randomUUID());
        program.setName("Distribution Test Program");
        program = programFiliereRepository.save(program);

        academicLevel = new AcademicLevel();
        academicLevel.setProgramFiliere(program);
        academicLevel.setName("M1");
        academicLevel.setLevelOrder(1);
        academicLevel = academicLevelRepository.save(academicLevel);

        academicYear = new AcademicYear();
        academicYear.setEstablishment(establishment);
        academicYear.setLabel("2030-2031");
        academicYear.setStartYear(2030);
        academicYear.setEndYear(2031);
        academicYear.setStatus(AcademicYearStatus.ACTIVE);
        academicYear = academicYearRepository.save(academicYear);

        Semester firstSemester = saveSemester("S1", 1);
        Semester secondSemester = saveSemester("S2", 2);
        registrations = createRegistrations(establishment, program, firstSemester, secondSemester, 8);
        root = new AuthenticatedUserPrincipal(
            UUID.randomUUID(),
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null,
            "root@distribution.test"
        );
    }

    @Test
    void generatesBalancedGroupsAndAssignsEverySemester() {
        AcademicLevel carriedModuleLevel = new AcademicLevel();
        carriedModuleLevel.setProgramFiliere(academicLevel.getProgramFiliere());
        carriedModuleLevel.setName("Carried module level");
        carriedModuleLevel.setLevelOrder(2);
        carriedModuleLevel = academicLevelRepository.save(carriedModuleLevel);

        Semester carriedModuleSemester = new Semester();
        carriedModuleSemester.setAcademicLevel(carriedModuleLevel);
        carriedModuleSemester.setAcademicYear(academicYear);
        carriedModuleSemester.setName("Carried S1");
        carriedModuleSemester.setSemesterOrder(1);
        carriedModuleSemester = semesterRepository.save(carriedModuleSemester);

        SemesterRegistration carriedRegistration = new SemesterRegistration();
        carriedRegistration.setAcademicRegistration(registrations.get(0));
        carriedRegistration.setSemester(carriedModuleSemester);
        carriedRegistration = semesterRegistrationRepository.save(carriedRegistration);

        ClassGroupGenerationResponse response = generationService.generateClassGroups(
            root,
            academicLevel.getId(),
            academicYear.getId(),
            new GenerateClassGroupsRequest(2, 3)
        );

        assertThat(response.totalStudents()).isEqualTo(8);
        assertThat(response.semesterAssignmentsCreated()).isEqualTo(16);
        assertThat(response.groups())
            .extracting(group -> group.studentCount())
            .containsExactly(3, 3, 2);
        assertThat(response.groups())
            .extracting(group -> group.name())
            .containsExactly("Group A", "Group B", "Group C");

        Map<UUID, UUID> groupByRegistration = new HashMap<>();
        for (StudentClassAssignment assignment : classAssignmentRepository.findAll()) {
            UUID registrationId = assignment.getSemesterRegistration()
                .getAcademicRegistration()
                .getId();
            UUID previousGroupId = groupByRegistration.putIfAbsent(
                registrationId,
                assignment.getClassGroup().getId()
            );
            if (previousGroupId != null) {
                assertThat(assignment.getClassGroup().getId()).isEqualTo(previousGroupId);
            }
        }
        assertThat(groupByRegistration).hasSize(8);
        assertThat(classAssignmentRepository.findBySemesterRegistrationId(
            carriedRegistration.getId()
        )).isEmpty();
    }

    @Test
    void bulkAssignsRegistrationsToManuallyCreatedGroups() {
        ClassGroupResponse groupA = classGroupService.createClassGroup(
            root,
            academicLevel.getId(),
            academicYear.getId(),
            new CreateClassGroupRequest("Group A", ClassGroupStatus.ACTIVE)
        );
        ClassGroupResponse groupB = classGroupService.createClassGroup(
            root,
            academicLevel.getId(),
            academicYear.getId(),
            new CreateClassGroupRequest("Group B", ClassGroupStatus.ACTIVE)
        );
        List<BulkClassAssignmentItemRequest> items = new ArrayList<>();
        for (int index = 0; index < registrations.size(); index++) {
            items.add(new BulkClassAssignmentItemRequest(
                registrations.get(index).getId(),
                index % 2 == 0 ? groupA.id() : groupB.id()
            ));
        }

        BulkClassAssignmentResponse firstResponse = classAssignmentService
            .bulkAssignStudentClasses(
                root,
                academicLevel.getId(),
                academicYear.getId(),
                new BulkAssignStudentClassesRequest(items)
            );
        BulkClassAssignmentResponse repeatedResponse = classAssignmentService
            .bulkAssignStudentClasses(
                root,
                academicLevel.getId(),
                academicYear.getId(),
                new BulkAssignStudentClassesRequest(items)
            );

        assertThat(firstResponse.studentsProcessed()).isEqualTo(8);
        assertThat(firstResponse.semesterAssignmentsCreated()).isEqualTo(16);
        assertThat(repeatedResponse.semesterAssignmentsCreated()).isZero();

        BulkClassAssignmentResponse movedResponse = classAssignmentService
            .bulkAssignStudentClasses(
                root,
                academicLevel.getId(),
                academicYear.getId(),
                new BulkAssignStudentClassesRequest(List.of(
                    new BulkClassAssignmentItemRequest(registrations.get(0).getId(), groupB.id())
                ))
            );

        assertThat(movedResponse.semesterAssignmentsCreated()).isEqualTo(2);
        assertThat(classAssignmentRepository.findAll().stream()
            .filter(assignment -> assignment.getSemesterRegistration()
                .getAcademicRegistration().getId().equals(registrations.get(0).getId())))
            .allMatch(assignment -> assignment.getClassGroup().getId().equals(groupB.id()));
    }

    @Test
    void rebalancesExistingGroupsAndMovesEverySemesterTogether() {
        generationService.generateClassGroups(
            root,
            academicLevel.getId(),
            academicYear.getId(),
            new GenerateClassGroupsRequest(2, 3)
        );

        ClassGroupRebalanceResponse response = generationService.rebalanceClassGroups(
            root,
            academicLevel.getId(),
            academicYear.getId(),
            new GenerateClassGroupsRequest(2, 4)
        );

        assertThat(response.groups()).hasSize(2);
        assertThat(response.groups())
            .extracting(group -> group.studentCount())
            .containsExactly(4, 4);
        assertThat(response.groups())
            .extracting(group -> group.name())
            .containsExactly("Group A", "Group B");
        assertThat(classGroupRepository
            .findByAcademicLevelIdAndAcademicYearIdOrderByNameAsc(
                academicLevel.getId(),
                academicYear.getId()
            ))
            .filteredOn(group -> group.getStatus() == ClassGroupStatus.INACTIVE)
            .hasSize(1);
        Map<UUID, UUID> groupByRegistration = new HashMap<>();
        classAssignmentRepository.findAll().forEach(assignment -> {
            UUID registrationId = assignment.getSemesterRegistration()
                .getAcademicRegistration().getId();
            UUID previous = groupByRegistration.putIfAbsent(
                registrationId,
                assignment.getClassGroup().getId()
            );
            if (previous != null) {
                assertThat(assignment.getClassGroup().getId()).isEqualTo(previous);
            }
        });
        assertThat(groupByRegistration).hasSize(8);

        ClassGroupRebalanceResponse expandedResponse = generationService.rebalanceClassGroups(
            root,
            academicLevel.getId(),
            academicYear.getId(),
            new GenerateClassGroupsRequest(2, 3)
        );

        assertThat(expandedResponse.groups())
            .extracting(group -> group.name())
            .containsExactly("Group A", "Group B", "Group C");
        assertThat(classGroupRepository
            .findByAcademicLevelIdAndAcademicYearIdOrderByNameAsc(
                academicLevel.getId(),
                academicYear.getId()
            ))
            .filteredOn(group -> group.getStatus() == ClassGroupStatus.ACTIVE)
            .extracting(ClassGroup::getName)
            .containsExactly("Group A", "Group B", "Group C");
    }

    @Test
    void returnsSemesterRosterWithGroupedAndUnassignedRegistrations() {
        Semester semester = semesterRepository
            .findByAcademicLevelIdAndAcademicYearIdOrderBySemesterOrderAsc(
                academicLevel.getId(),
                academicYear.getId()
            )
            .get(0);
        ClassGroupResponse group = classGroupService.createClassGroup(
            root,
            academicLevel.getId(),
            academicYear.getId(),
            new CreateClassGroupRequest("Group A", ClassGroupStatus.ACTIVE)
        );
        classAssignmentService.bulkAssignStudentClasses(
            root,
            academicLevel.getId(),
            academicYear.getId(),
            new BulkAssignStudentClassesRequest(List.of(
                new BulkClassAssignmentItemRequest(registrations.get(0).getId(), group.id()),
                new BulkClassAssignmentItemRequest(registrations.get(1).getId(), group.id())
            ))
        );

        ClassGroupRosterResponse roster = classAssignmentService.getClassGroupRoster(
            root,
            academicLevel.getId(),
            academicYear.getId(),
            semester.getId()
        );

        assertThat(roster.totalStudents()).isEqualTo(8);
        assertThat(roster.groups()).hasSize(1);
        assertThat(roster.groups().get(0).academicRegistrationIds())
            .containsExactly(registrations.get(0).getId(), registrations.get(1).getId());
        assertThat(roster.unassignedAcademicRegistrationIds())
            .containsExactlyElementsOf(registrations.subList(2, 8).stream()
                .map(AcademicRegistration::getId)
                .toList());
    }

    @Test
    void includesCarriedRegistrationFromAnotherAcademicLevelInSemesterRoster() {
        Semester semester = semesterRepository
            .findByAcademicLevelIdAndAcademicYearIdOrderBySemesterOrderAsc(
                academicLevel.getId(),
                academicYear.getId()
            )
            .get(1);
        ClassGroupResponse groupResponse = classGroupService.createClassGroup(
            root,
            academicLevel.getId(),
            academicYear.getId(),
            new CreateClassGroupRequest("Group A", ClassGroupStatus.ACTIVE)
        );
        ClassGroup group = classGroupRepository.findById(groupResponse.id()).orElseThrow();

        AcademicLevel nextLevel = new AcademicLevel();
        nextLevel.setProgramFiliere(academicLevel.getProgramFiliere());
        nextLevel.setName("M2");
        nextLevel.setLevelOrder(2);
        nextLevel = academicLevelRepository.save(nextLevel);

        UserAccount account = new UserAccount();
        account.setUniversityEmail("carried-student-" + UUID.randomUUID() + "@test.local");
        account.setPasswordHash("not-used");
        account.setRole(AccountRoleType.STUDENT);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account = userAccountRepository.save(account);

        Student student = new Student();
        student.setUserAccount(account);
        student.setEstablishment(academicLevel.getProgramFiliere().getDepartment().getEstablishment());
        student.setApogeeCode("CARRIED-" + UUID.randomUUID());
        student = studentRepository.save(student);

        AcademicRegistration registration = new AcademicRegistration();
        registration.setStudent(student);
        registration.setProgramFiliere(academicLevel.getProgramFiliere());
        registration.setAcademicYear(academicYear);
        registration.setAcademicLevel(nextLevel);
        registration.setStatus(AcademicRegistrationStatus.ACTIVE);
        registration = academicRegistrationRepository.save(registration);

        SemesterRegistration semesterRegistration = new SemesterRegistration();
        semesterRegistration.setAcademicRegistration(registration);
        semesterRegistration.setSemester(semester);
        semesterRegistration = semesterRegistrationRepository.save(semesterRegistration);

        StudentClassAssignment assignment = new StudentClassAssignment();
        assignment.setSemesterRegistration(semesterRegistration);
        assignment.setClassGroup(group);
        classAssignmentRepository.save(assignment);

        ClassGroupRosterResponse roster = classAssignmentService.getClassGroupRoster(
            root,
            academicLevel.getId(),
            academicYear.getId(),
            semester.getId()
        );

        assertThat(roster.totalStudents()).isEqualTo(9);
        assertThat(roster.groups().get(0).academicRegistrationIds())
            .contains(registration.getId());
        assertThat(roster.unassignedAcademicRegistrationIds())
            .doesNotContain(registration.getId());
    }

    private Semester saveSemester(String name, int order) {
        Semester semester = new Semester();
        semester.setAcademicLevel(academicLevel);
        semester.setAcademicYear(academicYear);
        semester.setName(name);
        semester.setSemesterOrder(order);
        return semesterRepository.save(semester);
    }

    private List<AcademicRegistration> createRegistrations(
        Establishment establishment,
        ProgramFiliere program,
        Semester firstSemester,
        Semester secondSemester,
        int count
    ) {
        List<AcademicRegistration> created = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            UserAccount account = new UserAccount();
            account.setUniversityEmail("distribution-student-" + UUID.randomUUID() + "@test.local");
            account.setPasswordHash("not-used");
            account.setRole(AccountRoleType.STUDENT);
            account.setAccountStatus(AccountStatus.ACTIVE);
            account = userAccountRepository.save(account);

            Student student = new Student();
            student.setUserAccount(account);
            student.setEstablishment(establishment);
            student.setApogeeCode(String.format("DIST-%03d-%s", index, UUID.randomUUID()));
            student = studentRepository.save(student);

            AcademicRegistration registration = new AcademicRegistration();
            registration.setStudent(student);
            registration.setProgramFiliere(program);
            registration.setAcademicYear(academicYear);
            registration.setAcademicLevel(academicLevel);
            registration.setStatus(AcademicRegistrationStatus.ACTIVE);
            registration = academicRegistrationRepository.save(registration);
            created.add(registration);

            SemesterRegistration first = new SemesterRegistration();
            first.setAcademicRegistration(registration);
            first.setSemester(firstSemester);
            semesterRegistrationRepository.save(first);

            SemesterRegistration second = new SemesterRegistration();
            second.setAcademicRegistration(registration);
            second.setSemester(secondSemester);
            semesterRegistrationRepository.save(second);
        }
        return created;
    }
}
