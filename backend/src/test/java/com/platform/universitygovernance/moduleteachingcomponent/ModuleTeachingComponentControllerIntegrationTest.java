package com.platform.universitygovernance.moduleteachingcomponent;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.platform.platform.infrastructure.security.JwtTokenService;
import com.platform.universitygovernance.academicdomain.domain.AcademicDomain;
import com.platform.universitygovernance.academicdomain.infrastructure.AcademicDomainRepository;
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
import com.platform.universitygovernance.moduleteachingcomponent.infrastructure.ModuleTeachingComponentRepository;
import com.platform.universitygovernance.moduleteachingcomponent.infrastructure.TeachingComponentDomainRepository;
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = PlatformApplication.class)
@ActiveProfiles("test")
class ModuleTeachingComponentControllerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private TeachingComponentDomainRepository componentDomainRepository;

    @Autowired
    private ModuleTeachingComponentRepository componentRepository;

    @Autowired
    private AcademicDomainRepository academicDomainRepository;

    @Autowired
    private SubjectModuleRepository subjectModuleRepository;

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

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private Establishment firstEstablishment;
    private Establishment secondEstablishment;
    private SubjectModule firstModule;
    private SubjectModule secondModule;

    @BeforeEach
    void setUp() {
        clearBusinessData();

        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);

        firstEstablishment = saveEstablishment(university, "ENSA Agadir", EstablishmentType.SCHOOL);
        secondEstablishment = saveEstablishment(
            university,
            "Faculty of Sciences",
            EstablishmentType.FACULTY
        );
        firstModule = saveSubjectModule(firstEstablishment, "IL", "Algorithms");
        secondModule = saveSubjectModule(secondEstablishment, "MATH", "Analysis");
    }

    @AfterEach
    void tearDown() {
        clearBusinessData();
    }

    @Test
    void rootCanReadAndReplaceACompleteTeachingConfiguration() throws Exception {
        AcademicDomain software = saveAcademicDomain(firstEstablishment, "SE", "Software Engineering");
        AcademicDomain databases = saveAcademicDomain(firstEstablishment, "DB", "Databases");
        AcademicDomain mathematics = saveAcademicDomain(secondEstablishment, "MATH", "Mathematics");
        String rootToken = token(
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null,
            "root@uiz.ac.ma"
        );

        HttpResponse<String> initialResponse = putJson(
            componentPath(firstModule),
            rootToken,
            initialConfiguration(software.getId(), databases.getId())
        );
        assertThat(initialResponse.statusCode()).isEqualTo(200);
        JsonNode initial = objectMapper.readTree(initialResponse.body());
        assertThat(initial).hasSize(2);
        assertThat(initial.get(0).get("componentType").asText()).isEqualTo("COURSE");
        assertThat(initial.get(1).get("componentType").asText()).isEqualTo("TP");
        assertThat(initial.get(1).get("maximumGroupSize").asInt()).isEqualTo(20);
        String courseId = initial.get(0).get("id").asText();

        HttpResponse<String> getResponse = get(componentPath(firstModule), rootToken);
        assertThat(getResponse.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(getResponse.body())).hasSize(2);

        HttpResponse<String> updatedResponse = putJson(
            componentPath(firstModule),
            rootToken,
            updatedConfiguration(software.getId())
        );
        assertThat(updatedResponse.statusCode()).isEqualTo(200);
        JsonNode updated = objectMapper.readTree(updatedResponse.body());
        assertThat(updated).hasSize(2);
        assertThat(updated.get(0).get("id").asText()).isEqualTo(courseId);
        assertThat(updated.get(0).get("sessionDurationMinutes").asInt()).isEqualTo(120);
        assertThat(updated.get(1).get("componentType").asText()).isEqualTo("TD");
        assertThat(componentRepository.findBySubjectModuleIdOrderByComponentTypeAsc(firstModule.getId()))
            .extracting(component -> component.getComponentType().name())
            .containsExactly("COURSE", "TD");

        assertThat(putJson(
            componentPath(firstModule),
            rootToken,
            crossEstablishmentConfiguration(mathematics.getId())
        ).statusCode()).isEqualTo(400);
        assertThat(componentRepository.findBySubjectModuleIdOrderByComponentTypeAsc(firstModule.getId()))
            .hasSize(2);

        assertThat(putJson(
            componentPath(firstModule),
            rootToken,
            duplicateTypeConfiguration()
        ).statusCode()).isEqualTo(400);
        assertThat(putJson(
            componentPath(firstModule),
            rootToken,
            subgroupWithoutSizeConfiguration()
        ).statusCode()).isEqualTo(400);
    }

    @Test
    void adminNeedsSpecificPermissionsAndMatchingEstablishment() throws Exception {
        AcademicDomain software = saveAcademicDomain(firstEstablishment, "SE", "Software Engineering");

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

        grant(admin, PermissionCode.MODULE_TEACHING_COMPONENT_UPDATE);
        String adminToken = jwtTokenService.generateAccessToken(
            account.getId(),
            AccountRoleType.ADMIN,
            admin.getId(),
            firstEstablishment.getId(),
            account.getUniversityEmail()
        );

        assertThat(putJson(
            componentPath(firstModule),
            adminToken,
            updatedConfiguration(software.getId())
        ).statusCode()).isEqualTo(200);
        assertThat(get(componentPath(firstModule), adminToken).statusCode()).isEqualTo(403);

        grant(admin, PermissionCode.MODULE_TEACHING_COMPONENT_VIEW);
        assertThat(get(componentPath(firstModule), adminToken).statusCode()).isEqualTo(200);
        assertThat(putJson(
            componentPath(secondModule),
            adminToken,
            "{\"components\":[]}"
        ).statusCode()).isEqualTo(403);
    }

    private String initialConfiguration(UUID softwareDomainId, UUID databaseDomainId) {
        return """
            {"components":[
              {"componentType":"COURSE","sessionsPerWeek":1,"sessionDurationMinutes":90,
               "audienceMode":"WHOLE_COHORT","maximumGroupSize":null,
               "requiredRoomType":"LECTURE_HALL","requiredDomainIds":["%s"]},
              {"componentType":"TP","sessionsPerWeek":1,"sessionDurationMinutes":120,
               "audienceMode":"SUBGROUP","maximumGroupSize":20,
               "requiredRoomType":"COMPUTER_LAB","requiredDomainIds":["%s","%s"]}
            ]}
            """.formatted(softwareDomainId, softwareDomainId, databaseDomainId);
    }

    private String updatedConfiguration(UUID softwareDomainId) {
        return """
            {"components":[
              {"componentType":"COURSE","sessionsPerWeek":1,"sessionDurationMinutes":120,
               "audienceMode":"WHOLE_COHORT","maximumGroupSize":null,
               "requiredRoomType":"LECTURE_HALL","requiredDomainIds":["%s"]},
              {"componentType":"TD","sessionsPerWeek":1,"sessionDurationMinutes":90,
               "audienceMode":"CLASS_GROUP","maximumGroupSize":null,
               "requiredRoomType":"CLASSROOM","requiredDomainIds":["%s"]}
            ]}
            """.formatted(softwareDomainId, softwareDomainId);
    }

    private String crossEstablishmentConfiguration(UUID academicDomainId) {
        return """
            {"components":[
              {"componentType":"COURSE","sessionsPerWeek":1,"sessionDurationMinutes":90,
               "audienceMode":"WHOLE_COHORT","maximumGroupSize":null,
               "requiredRoomType":"LECTURE_HALL","requiredDomainIds":["%s"]}
            ]}
            """.formatted(academicDomainId);
    }

    private String duplicateTypeConfiguration() {
        return """
            {"components":[
              {"componentType":"TD","sessionsPerWeek":1,"sessionDurationMinutes":90,
               "audienceMode":"CLASS_GROUP","maximumGroupSize":null,
               "requiredRoomType":"CLASSROOM","requiredDomainIds":[]},
              {"componentType":"TD","sessionsPerWeek":2,"sessionDurationMinutes":60,
               "audienceMode":"CLASS_GROUP","maximumGroupSize":null,
               "requiredRoomType":"CLASSROOM","requiredDomainIds":[]}
            ]}
            """;
    }

    private String subgroupWithoutSizeConfiguration() {
        return """
            {"components":[
              {"componentType":"TP","sessionsPerWeek":1,"sessionDurationMinutes":120,
               "audienceMode":"SUBGROUP","maximumGroupSize":null,
               "requiredRoomType":"COMPUTER_LAB","requiredDomainIds":[]}
            ]}
            """;
    }

    private void grant(Admin admin, PermissionCode permissionCode) {
        Permission permission = permissionRepository.findByCodeIn(Set.of(permissionCode)).get(0);
        AdminPermissionGrant grant = new AdminPermissionGrant();
        grant.setAdmin(admin);
        grant.setPermission(permission);
        adminPermissionGrantRepository.save(grant);
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

    private SubjectModule saveSubjectModule(
        Establishment establishment,
        String programCode,
        String title
    ) {
        Department department = new Department();
        department.setEstablishment(establishment);
        department.setName(programCode + " Department");
        department = departmentRepository.save(department);

        DegreeCycle degreeCycle = new DegreeCycle();
        degreeCycle.setEstablishment(establishment);
        degreeCycle.setName("Master " + programCode);
        degreeCycle = degreeCycleRepository.save(degreeCycle);

        ProgramPath programPath = new ProgramPath();
        programPath.setEstablishment(establishment);
        programPath.setName("Path " + programCode);
        programPath = programPathRepository.save(programPath);

        ProgramFiliere program = new ProgramFiliere();
        program.setDepartment(department);
        program.setDegreeCycle(degreeCycle);
        program.setProgramPath(programPath);
        program.setCode(programCode);
        program.setName(programCode + " Program");
        program = programFiliereRepository.save(program);

        AcademicLevel level = new AcademicLevel();
        level.setProgramFiliere(program);
        level.setName("M1");
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
        semester = semesterRepository.save(semester);

        SubjectModule subjectModule = new SubjectModule();
        subjectModule.setSemester(semester);
        subjectModule.setCode(programCode + "101");
        subjectModule.setTitle(title);
        return subjectModuleRepository.save(subjectModule);
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

    private void clearBusinessData() {
        adminPermissionGrantRepository.deleteAll();
        componentDomainRepository.deleteAll();
        componentRepository.deleteAll();
        academicDomainRepository.deleteAll();
        subjectModuleRepository.deleteAll();
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

    private String componentPath(SubjectModule subjectModule) {
        return "/api/v1/subject-modules/" + subjectModule.getId() + "/teaching-components";
    }

    private String token(
        AccountRoleType role,
        UUID roleEntityId,
        UUID establishmentId,
        String email
    ) {
        return jwtTokenService.generateAccessToken(
            UUID.randomUUID(),
            role,
            roleEntityId,
            establishmentId,
            email
        );
    }

    private HttpResponse<String> get(String path, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl() + path))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> putJson(String path, String token, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl() + path))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + token)
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
