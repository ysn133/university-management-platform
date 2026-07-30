package com.platform.universitygovernance.academicdomain;

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
import com.platform.universitygovernance.academicdomain.infrastructure.AcademicDomainRepository;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.domain.EstablishmentStatus;
import com.platform.universitygovernance.establishment.domain.EstablishmentType;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
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
class AcademicDomainControllerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private AcademicDomainRepository academicDomainRepository;

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
    void rootCanCreateListGetUpdateAndDeleteAcademicDomains() throws Exception {
        String rootToken = token(
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null,
            "root@uiz.ac.ma"
        );

        HttpResponse<String> createResponse = postJson(
            "/api/v1/establishments/" + firstEstablishment.getId() + "/academic-domains",
            rootToken,
            "{\"code\":\"  ml  \",\"name\":\"  Machine Learning  \"}"
        );
        assertThat(createResponse.statusCode()).isEqualTo(200);

        JsonNode created = objectMapper.readTree(createResponse.body());
        String academicDomainId = created.get("id").asText();
        assertThat(created.get("code").asText()).isEqualTo("ML");
        assertThat(created.get("name").asText()).isEqualTo("Machine Learning");
        assertThat(created.get("establishmentId").asText())
            .isEqualTo(firstEstablishment.getId().toString());

        assertThat(postJson(
            "/api/v1/establishments/" + firstEstablishment.getId() + "/academic-domains",
            rootToken,
            "{\"code\":\"ml\",\"name\":\"Duplicate\"}"
        ).statusCode()).isEqualTo(409);

        HttpResponse<String> listResponse = get(
            "/api/v1/establishments/" + firstEstablishment.getId() + "/academic-domains",
            rootToken
        );
        assertThat(listResponse.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(listResponse.body())).hasSize(1);

        HttpResponse<String> getResponse = get(
            "/api/v1/academic-domains/" + academicDomainId,
            rootToken
        );
        assertThat(getResponse.statusCode()).isEqualTo(200);

        HttpResponse<String> updateResponse = putJson(
            "/api/v1/academic-domains/" + academicDomainId,
            rootToken,
            "{\"code\":\"AI\",\"name\":\"Artificial Intelligence\"}"
        );
        assertThat(updateResponse.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(updateResponse.body()).get("code").asText())
            .isEqualTo("AI");

        HttpResponse<String> deleteResponse = delete(
            "/api/v1/academic-domains/" + academicDomainId,
            rootToken
        );
        assertThat(deleteResponse.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(deleteResponse.body()).get("success").asBoolean()).isTrue();
        assertThat(academicDomainRepository.findById(UUID.fromString(academicDomainId))).isEmpty();
    }

    @Test
    void adminNeedsSpecificPermissionAndCannotCrossEstablishments() throws Exception {
        String rootToken = token(
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null,
            "root@uiz.ac.ma"
        );
        String ownDomainId = createAcademicDomain(
            rootToken,
            firstEstablishment.getId(),
            "DB",
            "Databases"
        );
        String otherDomainId = createAcademicDomain(
            rootToken,
            secondEstablishment.getId(),
            "MATH",
            "Mathematics"
        );

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

        Permission updatePermission = permissionRepository
            .findByCodeIn(Set.of(PermissionCode.ACADEMIC_DOMAIN_UPDATE))
            .get(0);
        AdminPermissionGrant grant = new AdminPermissionGrant();
        grant.setAdmin(admin);
        grant.setPermission(updatePermission);
        adminPermissionGrantRepository.save(grant);

        String adminToken = jwtTokenService.generateAccessToken(
            account.getId(),
            AccountRoleType.ADMIN,
            admin.getId(),
            firstEstablishment.getId(),
            account.getUniversityEmail()
        );

        assertThat(get(
            "/api/v1/establishments/" + firstEstablishment.getId() + "/academic-domains",
            adminToken
        ).statusCode()).isEqualTo(403);
        assertThat(putJson(
            "/api/v1/academic-domains/" + ownDomainId,
            adminToken,
            "{\"code\":\"DATA\",\"name\":\"Data Engineering\"}"
        ).statusCode()).isEqualTo(200);
        assertThat(delete(
            "/api/v1/academic-domains/" + ownDomainId,
            adminToken
        ).statusCode()).isEqualTo(403);
        assertThat(putJson(
            "/api/v1/academic-domains/" + otherDomainId,
            adminToken,
            "{\"code\":\"SCI\",\"name\":\"Sciences\"}"
        ).statusCode()).isEqualTo(403);
    }

    private String createAcademicDomain(
        String token,
        UUID establishmentId,
        String code,
        String name
    ) throws Exception {
        HttpResponse<String> response = postJson(
            "/api/v1/establishments/" + establishmentId + "/academic-domains",
            token,
            "{\"code\":\"" + code + "\",\"name\":\"" + name + "\"}"
        );
        assertThat(response.statusCode()).isEqualTo(200);
        return objectMapper.readTree(response.body()).get("id").asText();
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
        academicDomainRepository.deleteAll();
        adminRepository.deleteAll();
        superAdminRepository.deleteAll();
        establishmentRepository.deleteAll();
        rootSuperAdminRepository.deleteAll();
        universityRepository.deleteAll();
        userProfileRepository.deleteAll();
        userAccountRepository.deleteAll();
    }

    private HttpResponse<String> get(String path, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl() + path))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postJson(String path, String token, String body) throws Exception {
        HttpRequest request = requestBuilder(path, token)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> putJson(String path, String token, String body) throws Exception {
        HttpRequest request = requestBuilder(path, token)
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> delete(String path, String token) throws Exception {
        HttpRequest request = requestBuilder(path, token).DELETE().build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.Builder requestBuilder(String path, String token) {
        return HttpRequest.newBuilder()
            .uri(URI.create(baseUrl() + path))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + token);
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
