package com.platform.scheduling.semesterschedule;

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
import com.platform.scheduling.semesterschedule.application.ScheduleEntryService;
import com.platform.scheduling.semesterschedule.application.SemesterScheduleService;
import com.platform.scheduling.semesterschedule.domain.SchedulePublicationStatus;
import com.platform.scheduling.semesterschedule.infrastructure.ScheduleEntryRepository;
import com.platform.scheduling.semesterschedule.infrastructure.SemesterScheduleRepository;
import com.platform.scheduling.semesterschedule.presentation.dto.CreateScheduleEntryRequest;
import com.platform.scheduling.semesterschedule.presentation.dto.CreateSemesterScheduleRequest;
import com.platform.scheduling.semesterschedule.presentation.dto.ScheduleEntryResponse;
import com.platform.scheduling.semesterschedule.presentation.dto.SemesterScheduleResponse;
import com.platform.scheduling.semesterschedule.presentation.dto.UpdateScheduleEntryRequest;
import com.platform.scheduling.domain.RoomType;
import com.platform.scheduling.teachinggroup.domain.TeachingGroup;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupRepository;
import com.platform.teachingassignment.domain.TeachingAssignment;
import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import com.platform.teachingassignment.infrastructure.TeachingAssignmentRepository;
import com.platform.teachingrequirement.domain.TeachingRequirement;
import com.platform.teachingrequirement.domain.TeachingRequirementStatus;
import com.platform.teachingrequirement.infrastructure.TeachingRequirementRepository;
import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academiclevel.infrastructure.AcademicLevelRepository;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicyear.domain.AcademicYearStatus;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.block.infrastructure.BlockRepository;
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
import com.platform.universitygovernance.room.domain.Room;
import com.platform.universitygovernance.room.domain.RoomStatus;
import com.platform.universitygovernance.room.infrastructure.RoomRepository;
import com.platform.universitygovernance.semester.domain.Semester;
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;
import com.platform.universitygovernance.semester.domain.SemesterTermType;
import com.platform.universitygovernance.subjectmodules.domain.SubjectModule;
import com.platform.universitygovernance.subjectmodules.infrastructure.SubjectModuleRepository;
import com.platform.universitygovernance.moduleteachingcomponent.domain.ModuleTeachingComponent;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import com.platform.universitygovernance.moduleteachingcomponent.infrastructure.ModuleTeachingComponentRepository;
import com.platform.universitygovernance.university.domain.University;
import com.platform.universitygovernance.university.infrastructure.UniversityRepository;
import java.time.DayOfWeek;
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
class SemesterScheduleServiceIntegrationTest {

    @Autowired
    private SemesterScheduleService semesterScheduleService;

    @Autowired
    private ScheduleEntryService scheduleEntryService;

    @Autowired
    private SemesterScheduleRepository semesterScheduleRepository;

    @Autowired
    private ScheduleEntryRepository scheduleEntryRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private TeachingAssignmentRepository teachingAssignmentRepository;

    @Autowired
    private TeachingRequirementRepository teachingRequirementRepository;

    @Autowired
    private TeachingGroupRepository teachingGroupRepository;

    @Autowired
    private ModuleTeachingComponentRepository teachingComponentRepository;

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
    private SemesterRepository semesterRepository;

    @Autowired
    private SubjectModuleRepository subjectModuleRepository;

    @Autowired
    private ClassGroupRepository classGroupRepository;

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
    private AcademicLevel academicLevel;

    @BeforeEach
    void setUp() {
        clearBusinessData();

        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);

        firstEstablishment = saveEstablishment(university, "ENSA Agadir");
        secondEstablishment = saveEstablishment(university, "Faculty of Sciences");
        ProgramFiliere program = saveProgram(firstEstablishment, "IL");
        academicLevel = saveLevel(program, "M1");
        academicYear = saveYear(firstEstablishment, "2026-2027", 2026);
        semester = saveSemester(academicLevel, academicYear, "S1");
    }

    @AfterEach
    void tearDown() {
        clearBusinessData();
    }

    @Test
    void rootCanCreateReadAndPublishSemesterSchedule() {
        AuthenticatedUserPrincipal root = principal(
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null
        );

        SemesterScheduleResponse created = semesterScheduleService.createSemesterSchedule(
            root,
            firstEstablishment.getId(),
            request()
        );

        assertThat(created.publicationStatus()).isEqualTo(SchedulePublicationStatus.DRAFT);
        assertThat(created.publishedAt()).isNull();
        assertThat(semesterScheduleService.getSemesterSchedule(root, created.id()).id())
            .isEqualTo(created.id());
        assertThat(semesterScheduleService.getSemesterSchedules(
            root,
            firstEstablishment.getId()
        ))
            .extracting(SemesterScheduleResponse::id)
            .containsExactly(created.id());

        SemesterScheduleResponse published = semesterScheduleService
            .publishSemesterSchedule(root, created.id());
        assertThat(published.publicationStatus())
            .isEqualTo(SchedulePublicationStatus.PUBLISHED);
        assertThat(published.publishedAt()).isNotNull();

        assertThatThrownBy(() -> semesterScheduleService.createSemesterSchedule(
            root,
            firstEstablishment.getId(),
            request()
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");

        assertThatThrownBy(() -> semesterScheduleService.createSemesterSchedule(
            root,
            secondEstablishment.getId(),
            request()
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");
    }

    @Test
    void adminNeedsIndependentSchedulePermissionsAndMatchingEstablishment() {
        Admin admin = saveAdmin(firstEstablishment);
        grant(admin, PermissionCode.SEMESTER_SCHEDULE_CREATE);
        AuthenticatedUserPrincipal principal = principal(
            AccountRoleType.ADMIN,
            admin.getId(),
            firstEstablishment.getId()
        );

        SemesterScheduleResponse created = semesterScheduleService.createSemesterSchedule(
            principal,
            firstEstablishment.getId(),
            request()
        );

        assertThatThrownBy(() -> semesterScheduleService.getSemesterSchedule(
            principal,
            created.id()
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");

        grant(admin, PermissionCode.SEMESTER_SCHEDULE_VIEW);
        assertThat(semesterScheduleService.getSemesterSchedule(principal, created.id()).id())
            .isEqualTo(created.id());

        assertThatThrownBy(() -> semesterScheduleService.publishSemesterSchedule(
            principal,
            created.id()
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");

        grant(admin, PermissionCode.SEMESTER_SCHEDULE_PUBLISH);
        assertThat(semesterScheduleService.publishSemesterSchedule(principal, created.id())
            .publicationStatus()).isEqualTo(SchedulePublicationStatus.PUBLISHED);

        assertThatThrownBy(() -> semesterScheduleService.getSemesterSchedules(
            principal,
            secondEstablishment.getId()
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
    }

    @Test
    void rootCanManageDraftEntriesAndPublicationFreezesThem() {
        AuthenticatedUserPrincipal root = principal(
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null
        );
        SemesterScheduleResponse schedule = semesterScheduleService
            .createSemesterSchedule(root, firstEstablishment.getId(), request());
        TeachingAssignment assignment = saveTeachingAssignment();
        Room roomA = saveRoom("A", "Room A", RoomType.CLASSROOM, 100);
        Room roomB = saveRoom("B", "Room B", RoomType.LECTURE_HALL, 100);

        ScheduleEntryResponse created = scheduleEntryService.createScheduleEntry(
            root,
            schedule.id(),
            new CreateScheduleEntryRequest(
                assignment.getId(),
                DayOfWeek.MONDAY,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                roomA.getId()
            )
        );

        assertThat(created.roomId()).isEqualTo(roomA.getId());
        assertThat(created.roomName()).isEqualTo("Room A");
        assertThat(created.sourceClassGroupId()).isEqualTo(
            assignment.getTeachingRequirement().getTeachingGroup()
                .getSourceClassGroup().getId()
        );
        assertThat(created.audienceType()).isEqualTo(TeachingAudienceMode.CLASS_GROUP);
        assertThat(scheduleEntryService.getScheduleEntry(root, created.id()).id())
            .isEqualTo(created.id());
        assertThat(scheduleEntryService.getScheduleEntries(root, schedule.id()))
            .extracting(ScheduleEntryResponse::id)
            .containsExactly(created.id());

        ScheduleEntryResponse updated = scheduleEntryService.updateScheduleEntry(
            root,
            created.id(),
            new UpdateScheduleEntryRequest(
                assignment.getId(),
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                roomB.getId()
            )
        );
        assertThat(updated.startTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(updated.roomId()).isEqualTo(roomB.getId());

        assertThatThrownBy(() -> scheduleEntryService.createScheduleEntry(
            root,
            schedule.id(),
            new CreateScheduleEntryRequest(
                assignment.getId(),
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                roomA.getId()
            )
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");

        assertThatThrownBy(() -> scheduleEntryService.createScheduleEntry(
            root,
            schedule.id(),
            new CreateScheduleEntryRequest(
                assignment.getId(),
                DayOfWeek.TUESDAY,
                LocalTime.of(12, 0),
                LocalTime.of(11, 0),
                roomA.getId()
            )
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");

        semesterScheduleService.publishSemesterSchedule(root, schedule.id());
        assertThat(scheduleEntryService.deleteScheduleEntry(
            root,
            created.id()
        ).success()).isTrue();
        assertThat(scheduleEntryService.getScheduleEntries(root, schedule.id())).isEmpty();
    }

    private CreateSemesterScheduleRequest request() {
        return new CreateSemesterScheduleRequest(academicYear.getId(), semester.getId());
    }

    private TeachingAssignment saveTeachingAssignment() {
        Professor professor = saveProfessor("professor@ensa.uiz.ac.ma");
        SubjectModule subjectModule = saveSubjectModule();
        ClassGroup classGroup = saveClassGroup();

        TeachingAssignment assignment = new TeachingAssignment();
        assignment.setProfessor(professor);
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

        TeachingRequirement requirement = new TeachingRequirement();
        requirement.setModuleTeachingComponent(component);
        requirement.setTeachingGroup(teachingGroup);
        requirement.setStatus(TeachingRequirementStatus.ACTIVE);
        requirement = teachingRequirementRepository.save(requirement);

        assignment.setTeachingRequirement(requirement);
        assignment.setStatus(TeachingAssignmentStatus.ACTIVE);
        return teachingAssignmentRepository.save(assignment);
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
        professor.setEstablishment(firstEstablishment);
        professor.setEmployeeNumber("EMP-" + UUID.randomUUID());
        professor.setMaximumWeeklyTeachingMinutes(480);
        return professorRepository.save(professor);
    }

    private SubjectModule saveSubjectModule() {
        SubjectModule subjectModule = new SubjectModule();
        subjectModule.setSemester(semester);
        subjectModule.setCode("ALG");
        subjectModule.setTitle("Algorithms");
        return subjectModuleRepository.save(subjectModule);
    }

    private ClassGroup saveClassGroup() {
        ClassGroup classGroup = new ClassGroup();
        classGroup.setAcademicLevel(academicLevel);
        classGroup.setAcademicYear(academicYear);
        classGroup.setName("Group A");
        classGroup.setStatus(ClassGroupStatus.ACTIVE);
        return classGroupRepository.save(classGroup);
    }

    private Room saveRoom(
        String code,
        String name,
        RoomType roomType,
        int capacity
    ) {
        Room room = new Room();
        room.setEstablishment(firstEstablishment);
        room.setCode(code);
        room.setName(name);
        room.setRoomType(roomType);
        room.setCapacity(capacity);
        room.setStatus(RoomStatus.ACTIVE);
        return roomRepository.save(room);
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
        AcademicLevel level,
        AcademicYear year,
        String name
    ) {
        Semester savedSemester = new Semester();
        savedSemester.setAcademicLevel(level);
        savedSemester.setAcademicYear(year);
        savedSemester.setName(name);
        savedSemester.setSemesterOrder(1);
        savedSemester.setTermType(SemesterTermType.AUTUMN);
        return semesterRepository.save(savedSemester);
    }

    private void clearBusinessData() {
        scheduleEntryRepository.deleteAll();
        teachingAssignmentRepository.deleteAll();
        teachingRequirementRepository.deleteAll();
        teachingGroupRepository.deleteAll();
        teachingComponentRepository.deleteAll();
        semesterScheduleRepository.deleteAll();
        roomRepository.deleteAll();
        blockRepository.deleteAll();
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
