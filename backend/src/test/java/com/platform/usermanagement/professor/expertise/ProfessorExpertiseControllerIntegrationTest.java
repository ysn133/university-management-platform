package com.platform.usermanagement.professor.expertise;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.platform.identityaccess.infrastructure.SuperAdminRepository;
import com.platform.identityaccess.infrastructure.UserAccountRepository;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.PlatformApplication;
import com.platform.platform.infrastructure.security.JwtTokenService;
import com.platform.universitygovernance.academicdomain.domain.AcademicDomain;
import com.platform.universitygovernance.academicdomain.infrastructure.AcademicDomainRepository;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.domain.EstablishmentStatus;
import com.platform.universitygovernance.establishment.domain.EstablishmentType;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.universitygovernance.university.domain.University;
import com.platform.universitygovernance.university.infrastructure.UniversityRepository;
import com.platform.usermanagement.professor.expertise.infrastructure.ProfessorExpertiseRepository;
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
class ProfessorExpertiseControllerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private ProfessorExpertiseRepository professorExpertiseRepository;

    @Autowired
    private AcademicDomainRepository academicDomainRepository;

    @Autowired
    private ProfessorRepository professorRepository;

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
    }

    @AfterEach
    void tearDown() {
        clearBusinessData();
    }

    @Test
    void managementCanReplaceExpertiseAndProfessorCanReadOwnExpertise() throws Exception {
        Professor professor = saveProfessor(firstEstablishment, "professor@uiz.ac.ma", "EMP-1001");
        AcademicDomain databases = saveAcademicDomain(firstEstablishment, "DB", "Databases");
        AcademicDomain machineLearning = saveAcademicDomain(
            firstEstablishment,
            "ML",
            "Machine Learning"
        );
        AcademicDomain mathematics = saveAcademicDomain(secondEstablishment, "MATH", "Mathematics");
        String rootToken = token(
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null,
            "root@uiz.ac.ma"
        );

        HttpResponse<String> replaceResponse = putJson(
            expertisePath(professor),
            rootToken,
            expertiseBody(databases.getId(), machineLearning.getId())
        );
        assertThat(replaceResponse.statusCode()).isEqualTo(200);
        JsonNode replaced = objectMapper.readTree(replaceResponse.body());
        assertThat(replaced.get("professorId").asText()).isEqualTo(professor.getId().toString());
        assertThat(replaced.get("academicDomains")).hasSize(2);
        assertThat(replaced.get("academicDomains").get(0).get("code").asText()).isEqualTo("DB");
        assertThat(replaced.get("academicDomains").get(1).get("code").asText()).isEqualTo("ML");

        String professorToken = token(
            AccountRoleType.PROFESSOR,
            professor.getId(),
            firstEstablishment.getId(),
            professor.getUserAccount().getUniversityEmail()
        );
        assertThat(get(expertisePath(professor), professorToken).statusCode()).isEqualTo(200);
        assertThat(putJson(
            expertisePath(professor),
            professorToken,
            expertiseBody(databases.getId())
        ).statusCode()).isEqualTo(403);

        assertThat(putJson(
            expertisePath(professor),
            rootToken,
            expertiseBody(mathematics.getId())
        ).statusCode()).isEqualTo(400);
        assertThat(professorExpertiseRepository.count()).isEqualTo(2);

        HttpResponse<String> clearResponse = putJson(
            expertisePath(professor),
            rootToken,
            expertiseBody()
        );
        assertThat(clearResponse.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(clearResponse.body()).get("academicDomains")).isEmpty();
    }

    @Test
    void adminNeedsSpecificPermissionsAndMatchingEstablishment() throws Exception {
        Professor professor = saveProfessor(firstEstablishment, "professor@uiz.ac.ma", "EMP-1001");
        Professor otherProfessor = saveProfessor(
            secondEstablishment,
            "other.professor@uiz.ac.ma",
            "EMP-2001"
        );
        AcademicDomain databases = saveAcademicDomain(firstEstablishment, "DB", "Databases");

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

        grant(admin, PermissionCode.PROFESSOR_EXPERTISE_UPDATE);
        String adminToken = jwtTokenService.generateAccessToken(
            account.getId(),
            AccountRoleType.ADMIN,
            admin.getId(),
            firstEstablishment.getId(),
            account.getUniversityEmail()
        );

        assertThat(putJson(
            expertisePath(professor),
            adminToken,
            expertiseBody(databases.getId())
        ).statusCode()).isEqualTo(200);
        assertThat(get(expertisePath(professor), adminToken).statusCode()).isEqualTo(403);

        grant(admin, PermissionCode.PROFESSOR_EXPERTISE_VIEW);
        assertThat(get(expertisePath(professor), adminToken).statusCode()).isEqualTo(200);
        assertThat(putJson(
            expertisePath(otherProfessor),
            adminToken,
            expertiseBody()
        ).statusCode()).isEqualTo(403);
    }

    private void grant(Admin admin, PermissionCode permissionCode) {
        Permission permission = permissionRepository.findByCodeIn(Set.of(permissionCode)).get(0);
        AdminPermissionGrant grant = new AdminPermissionGrant();
        grant.setAdmin(admin);
        grant.setPermission(permission);
        adminPermissionGrantRepository.save(grant);
    }

    private Professor saveProfessor(
        Establishment establishment,
        String email,
        String employeeNumber
    ) {
        UserAccount account = new UserAccount();
        account.setUniversityEmail(email);
        account.setPasswordHash("not-used-by-this-test");
        account.setRole(AccountRoleType.PROFESSOR);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account = userAccountRepository.save(account);

        Professor professor = new Professor();
        professor.setUserAccount(account);
        professor.setEstablishment(establishment);
        professor.setEmployeeNumber(employeeNumber);
        professor.setMaximumWeeklyTeachingMinutes(480);
        return professorRepository.save(professor);
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
        professorExpertiseRepository.deleteAll();
        academicDomainRepository.deleteAll();
        professorRepository.deleteAll();
        adminRepository.deleteAll();
        superAdminRepository.deleteAll();
        establishmentRepository.deleteAll();
        rootSuperAdminRepository.deleteAll();
        universityRepository.deleteAll();
        userProfileRepository.deleteAll();
        userAccountRepository.deleteAll();
    }

    private String expertisePath(Professor professor) {
        return "/api/v1/professors/" + professor.getId() + "/expertise";
    }

    private String expertiseBody(UUID... academicDomainIds) {
        String ids = java.util.Arrays.stream(academicDomainIds)
            .map(id -> "\"" + id + "\"")
            .collect(java.util.stream.Collectors.joining(","));
        return "{\"academicDomainIds\":[" + ids + "]}";
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
