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
import com.platform.moduleclassresponsibility.application.ModuleClassResponsibilityService;
import com.platform.moduleclassresponsibility.domain.ModuleClassResponsibilityStatus;
import com.platform.moduleclassresponsibility.infrastructure.ModuleClassResponsibilityRepository;
import com.platform.moduleclassresponsibility.presentation.dto.CreateModuleClassResponsibilityRequest;
import com.platform.scheduling.domain.RoomType;
import com.platform.scheduling.teachinggroup.domain.TeachingGroup;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupRepository;
import com.platform.shared.presentation.ActionResponse;
import com.platform.teachingassignment.application.TeachingAssignmentService;
import com.platform.teachingassignment.application.TeachingAssignmentGenerationService;
import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import com.platform.teachingassignment.domain.TeachingAssignmentSource;
import com.platform.teachingassignment.infrastructure.TeachingAssignmentRepository;
import com.platform.teachingassignment.presentation.dto.CreateTeachingAssignmentRequest;
import com.platform.teachingassignment.presentation.dto.TeachingAssignmentResponse;
import com.platform.teachingrequirement.domain.TeachingRequirement;
import com.platform.teachingrequirement.domain.TeachingRequirementStatus;
import com.platform.teachingrequirement.infrastructure.TeachingRequirementRepository;
import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academiclevel.infrastructure.AcademicLevelRepository;
import com.platform.universitygovernance.academicdomain.domain.AcademicDomain;
import com.platform.universitygovernance.academicdomain.infrastructure.AcademicDomainRepository;
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
import com.platform.universitygovernance.semester.domain.SemesterTermType;
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;
import com.platform.universitygovernance.subjectmodules.domain.SubjectModule;
import com.platform.universitygovernance.subjectmodules.domain.SubjectModuleDomain;
import com.platform.universitygovernance.subjectmodules.infrastructure.SubjectModuleDomainRepository;
import com.platform.universitygovernance.subjectmodules.infrastructure.SubjectModuleRepository;
import com.platform.universitygovernance.moduleteachingcomponent.domain.ModuleTeachingComponent;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import com.platform.universitygovernance.moduleteachingcomponent.infrastructure.ModuleTeachingComponentRepository;
import com.platform.universitygovernance.university.domain.University;
import com.platform.universitygovernance.university.infrastructure.UniversityRepository;
import com.platform.usermanagement.professor.expertise.domain.ProfessorExpertise;
import com.platform.usermanagement.professor.expertise.infrastructure.ProfessorExpertiseRepository;
import com.platform.usermanagement.professor.rank.domain.AcademicRank;
import com.platform.usermanagement.professor.rank.domain.AcademicRankStatus;
import com.platform.usermanagement.professor.rank.infrastructure.AcademicRankRepository;
import com.platform.teachingassignment.rankpreference.domain.TeachingAssignmentRankPreference;
import com.platform.teachingassignment.rankpreference.infrastructure.TeachingAssignmentRankPreferenceRepository;
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
    private TeachingAssignmentGenerationService teachingAssignmentGenerationService;

    @Autowired
    private ModuleClassResponsibilityService responsibilityService;

    @Autowired
    private ModuleClassResponsibilityRepository responsibilityRepository;

    @Autowired
    private TeachingAssignmentRepository teachingAssignmentRepository;

    @Autowired
    private TeachingRequirementRepository teachingRequirementRepository;

    @Autowired
    private TeachingGroupRepository teachingGroupRepository;

    @Autowired
    private ModuleTeachingComponentRepository teachingComponentRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private SubjectModuleRepository subjectModuleRepository;

    @Autowired
    private SubjectModuleDomainRepository subjectModuleDomainRepository;

    @Autowired
    private AcademicDomainRepository academicDomainRepository;

    @Autowired
    private ProfessorExpertiseRepository professorExpertiseRepository;

    @Autowired
    private AcademicRankRepository academicRankRepository;

    @Autowired
    private TeachingAssignmentRankPreferenceRepository rankPreferenceRepository;

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
    private TeachingRequirement teachingRequirement;
    private AcademicRank defaultProfessorRank;

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

        defaultProfessorRank = saveRank(
            "DEFAULT_PROFESSOR",
            "Default Professor",
            1,
            true
        );

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

        ModuleTeachingComponent component = new ModuleTeachingComponent();
        component.setSubjectModule(subjectModule);
        component.setComponentType(TeachingComponentType.COURSE);
        component.setSessionsPerWeek(1);
        component.setSessionDurationMinutes(60);
        component.setAudienceMode(TeachingAudienceMode.CLASS_GROUP);
        component.setRequiredRoomType(RoomType.CLASSROOM);
        component = teachingComponentRepository.save(component);

        TeachingGroup teachingGroup = new TeachingGroup();
        teachingGroup.setSemester(semester);
        teachingGroup.setSourceClassGroup(classGroup);
        teachingGroup.setName(classGroup.getName());
        teachingGroup.setAudienceType(TeachingAudienceMode.CLASS_GROUP);
        teachingGroup = teachingGroupRepository.save(teachingGroup);

        teachingRequirement = new TeachingRequirement();
        teachingRequirement.setModuleTeachingComponent(component);
        teachingRequirement.setTeachingGroup(teachingGroup);
        teachingRequirement.setStatus(TeachingRequirementStatus.ACTIVE);
        teachingRequirement = teachingRequirementRepository.save(teachingRequirement);
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
        assertThat(responsibilityRepository
            .findBySubjectModuleIdAndClassGroupIdAndAcademicYearIdAndSemesterIdAndStatus(
                subjectModule.getId(),
                classGroup.getId(),
                academicYear.getId(),
                semester.getId(),
                ModuleClassResponsibilityStatus.ACTIVE
            )
            .orElseThrow()
            .getProfessor()
            .getId()).isEqualTo(firstProfessor.getId());
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
        assertThat(responsibilityRepository
            .findBySubjectModuleIdAndClassGroupIdAndAcademicYearIdAndSemesterIdAndStatus(
                subjectModule.getId(),
                classGroup.getId(),
                academicYear.getId(),
                semester.getId(),
                ModuleClassResponsibilityStatus.ACTIVE
            )
            .orElseThrow()
            .getProfessor()
            .getId()).isEqualTo(secondProfessor.getId());
    }

    @Test
    void workloadIncludesOtherSemestersInTheSameTeachingPeriod() {
        AuthenticatedUserPrincipal root = principal(
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null
        );
        Professor professor = saveProfessor("period.workload@uiz.ac.ma");
        professor.setMaximumWeeklyTeachingMinutes(60);
        professorRepository.save(professor);

        teachingAssignmentService.createTeachingAssignment(
            root,
            establishment.getId(),
            request(professor)
        );

        Semester parallelSemester = new Semester();
        parallelSemester.setAcademicLevel(semester.getAcademicLevel());
        parallelSemester.setAcademicYear(academicYear);
        parallelSemester.setName("S3");
        parallelSemester.setSemesterOrder(3);
        parallelSemester.setTermType(SemesterTermType.AUTUMN);
        parallelSemester = semesterRepository.save(parallelSemester);

        SubjectModule parallelModule = new SubjectModule();
        parallelModule.setSemester(parallelSemester);
        parallelModule.setCode("PAR");
        parallelModule.setTitle("Parallel Module");
        parallelModule = subjectModuleRepository.save(parallelModule);

        ModuleTeachingComponent parallelComponent = new ModuleTeachingComponent();
        parallelComponent.setSubjectModule(parallelModule);
        parallelComponent.setComponentType(TeachingComponentType.COURSE);
        parallelComponent.setSessionsPerWeek(1);
        parallelComponent.setSessionDurationMinutes(60);
        parallelComponent.setAudienceMode(TeachingAudienceMode.CLASS_GROUP);
        parallelComponent.setRequiredRoomType(RoomType.CLASSROOM);
        parallelComponent = teachingComponentRepository.save(parallelComponent);

        TeachingGroup parallelGroup = new TeachingGroup();
        parallelGroup.setSemester(parallelSemester);
        parallelGroup.setSourceClassGroup(classGroup);
        parallelGroup.setName("Parallel Group");
        parallelGroup.setAudienceType(TeachingAudienceMode.CLASS_GROUP);
        parallelGroup = teachingGroupRepository.save(parallelGroup);

        TeachingRequirement parallelRequirement = new TeachingRequirement();
        parallelRequirement.setModuleTeachingComponent(parallelComponent);
        parallelRequirement.setTeachingGroup(parallelGroup);
        parallelRequirement.setStatus(TeachingRequirementStatus.ACTIVE);
        parallelRequirement = teachingRequirementRepository.save(parallelRequirement);

        UUID parallelRequirementId = parallelRequirement.getId();
        assertThatThrownBy(() -> teachingAssignmentService.createTeachingAssignment(
            root,
            establishment.getId(),
            new CreateTeachingAssignmentRequest(professor.getId(), parallelRequirementId)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("weekly workload");
    }

    @Test
    void moduleAndClassHaveOneActiveResponsibleProfessor() {
        AuthenticatedUserPrincipal root = principal(
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null
        );
        Professor firstProfessor = saveProfessor("responsible.one@uiz.ac.ma");
        Professor secondProfessor = saveProfessor("responsible.two@uiz.ac.ma");

        var first = responsibilityService.createResponsibility(
            root,
            establishment.getId(),
            responsibilityRequest(firstProfessor)
        );
        assertThat(first.status()).isEqualTo(ModuleClassResponsibilityStatus.ACTIVE);

        assertThatThrownBy(() -> responsibilityService.createResponsibility(
            root,
            establishment.getId(),
            responsibilityRequest(secondProfessor)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");

        responsibilityService.removeResponsibility(root, first.id());
        assertThat(responsibilityService.createResponsibility(
            root,
            establishment.getId(),
            responsibilityRequest(secondProfessor)
        ).professorId()).isEqualTo(secondProfessor.getId());
    }

    @Test
    void professorMustCoverEverySubjectModuleDomain() {
        AuthenticatedUserPrincipal root = principal(
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null
        );
        AcademicDomain algorithms = new AcademicDomain();
        algorithms.setEstablishment(establishment);
        algorithms.setCode("ALG");
        algorithms.setName("Algorithms");
        algorithms = academicDomainRepository.save(algorithms);

        SubjectModuleDomain moduleDomain = new SubjectModuleDomain();
        moduleDomain.setSubjectModule(subjectModule);
        moduleDomain.setAcademicDomain(algorithms);
        subjectModuleDomainRepository.save(moduleDomain);

        Professor professor = saveProfessor("qualified.professor@uiz.ac.ma");
        assertThatThrownBy(() -> teachingAssignmentService.createTeachingAssignment(
            root,
            establishment.getId(),
            request(professor)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");

        ProfessorExpertise expertise = new ProfessorExpertise();
        expertise.setProfessor(professor);
        expertise.setAcademicDomain(algorithms);
        professorExpertiseRepository.save(expertise);

        assertThat(teachingAssignmentService.createTeachingAssignment(
            root,
            establishment.getId(),
            request(professor)
        ).professorId()).isEqualTo(professor.getId());
    }

    @Test
    void automaticGenerationUsesRankPreferenceAndPreservesItsAssignment() {
        AuthenticatedUserPrincipal root = principal(
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null
        );
        AcademicDomain algorithms = new AcademicDomain();
        algorithms.setEstablishment(establishment);
        algorithms.setCode("ALG");
        algorithms.setName("Algorithms");
        algorithms = academicDomainRepository.save(algorithms);

        SubjectModuleDomain moduleDomain = new SubjectModuleDomain();
        moduleDomain.setSubjectModule(subjectModule);
        moduleDomain.setAcademicDomain(algorithms);
        subjectModuleDomainRepository.save(moduleDomain);

        AcademicRank professorRank = saveRank("PROFESSOR", "Professor", 1, true);
        AcademicRank associateRank = saveRank(
            "ASSOCIATE_PROFESSOR",
            "Associate Professor",
            2,
            true
        );
        Professor professor = saveProfessor("professor@uiz.ac.ma");
        professor.setAcademicRank(professorRank);
        professorRepository.save(professor);
        Professor associate = saveProfessor("associate@uiz.ac.ma");
        associate.setAcademicRank(associateRank);
        professorRepository.save(associate);
        saveExpertise(professor, algorithms);
        saveExpertise(associate, algorithms);

        TeachingAssignmentRankPreference preference = new TeachingAssignmentRankPreference();
        preference.setEstablishment(establishment);
        preference.setComponentType(TeachingComponentType.COURSE);
        preference.setAcademicRank(associateRank);
        preference.setPriority(1);
        preference.setStatus(AcademicRankStatus.ACTIVE);
        rankPreferenceRepository.save(preference);

        var firstRun = teachingAssignmentGenerationService.generate(root, semester.getId());

        assertThat(firstRun.createdAssignments()).hasSize(1);
        assertThat(firstRun.createdAssignments().get(0).professorId())
            .isEqualTo(associate.getId());
        assertThat(firstRun.createdAssignments().get(0).assignmentSource())
            .isEqualTo(TeachingAssignmentSource.AUTOMATIC);
        assertThat(responsibilityRepository
            .findBySubjectModuleIdAndClassGroupIdAndAcademicYearIdAndSemesterIdAndStatus(
                subjectModule.getId(),
                classGroup.getId(),
                academicYear.getId(),
                semester.getId(),
                ModuleClassResponsibilityStatus.ACTIVE
            )
            .orElseThrow()
            .getProfessor()
            .getId()).isEqualTo(associate.getId());
        assertThat(firstRun.unresolvedRequirements()).isEmpty();

        var secondRun = teachingAssignmentGenerationService.generate(root, semester.getId());
        assertThat(secondRun.createdAssignments()).isEmpty();
        assertThat(secondRun.preservedAssignmentCount()).isEqualTo(1);

        ActionResponse cleared = teachingAssignmentGenerationService.clear(root, semester.getId());
        assertThat(cleared.success()).isTrue();
        assertThat(teachingAssignmentRepository.findById(firstRun.createdAssignments().get(0).id())
            .orElseThrow().getStatus()).isEqualTo(TeachingAssignmentStatus.INACTIVE);

        var regenerated = teachingAssignmentGenerationService.generate(root, semester.getId());
        assertThat(regenerated.createdAssignments()).hasSize(1);
        assertThat(regenerated.preservedAssignmentCount()).isZero();
    }

    private CreateTeachingAssignmentRequest request(Professor professor) {
        return new CreateTeachingAssignmentRequest(
            professor.getId(),
            teachingRequirement.getId()
        );
    }

    private CreateModuleClassResponsibilityRequest responsibilityRequest(
        Professor professor
    ) {
        return new CreateModuleClassResponsibilityRequest(
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
        professor.setAcademicRank(defaultProfessorRank);
        professor.setEmployeeNumber("EMP-" + UUID.randomUUID());
        professor.setMaximumWeeklyTeachingMinutes(480);
        return professorRepository.save(professor);
    }

    private AcademicRank saveRank(
        String code,
        String name,
        int seniorityOrder,
        boolean canHoldResponsibility
    ) {
        AcademicRank rank = new AcademicRank();
        rank.setEstablishment(establishment);
        rank.setCode(code);
        rank.setName(name);
        rank.setSeniorityOrder(seniorityOrder);
        rank.setCanHoldModuleResponsibility(canHoldResponsibility);
        rank.setStatus(AcademicRankStatus.ACTIVE);
        return academicRankRepository.save(rank);
    }

    private void saveExpertise(Professor professor, AcademicDomain domain) {
        ProfessorExpertise expertise = new ProfessorExpertise();
        expertise.setProfessor(professor);
        expertise.setAcademicDomain(domain);
        professorExpertiseRepository.save(expertise);
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
        rankPreferenceRepository.deleteAll();
        responsibilityRepository.deleteAll();
        teachingRequirementRepository.deleteAll();
        teachingGroupRepository.deleteAll();
        teachingComponentRepository.deleteAll();
        adminPermissionGrantRepository.deleteAll();
        professorExpertiseRepository.deleteAll();
        subjectModuleDomainRepository.deleteAll();
        subjectModuleRepository.deleteAll();
        academicDomainRepository.deleteAll();
        classGroupRepository.deleteAll();
        professorRepository.deleteAll();
        academicRankRepository.deleteAll();
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
