package com.platform.universitygovernance.department;

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
import com.platform.universitygovernance.department.infrastructure.DepartmentRepository;
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
import java.util.UUID;
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
class DepartmentControllerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenService jwtTokenService;

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

    @BeforeEach
    void setUp() {
        adminPermissionGrantRepository.deleteAll();
        departmentRepository.deleteAll();
        adminRepository.deleteAll();
        superAdminRepository.deleteAll();
        establishmentRepository.deleteAll();
        rootSuperAdminRepository.deleteAll();
        universityRepository.deleteAll();
        userProfileRepository.deleteAll();
        userAccountRepository.deleteAll();

        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);

        firstEstablishment = saveEstablishment(university, "ENSA Agadir", EstablishmentType.SCHOOL);
        secondEstablishment = saveEstablishment(university, "Faculty of Sciences", EstablishmentType.FACULTY);
    }

    @Test
    void rootCanCreateListAndUpdateDepartments() throws Exception {
        String rootToken = token(
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null,
            "root@uiz.ac.ma"
        );

        HttpResponse<String> createResponse = postJson(
            "/api/v1/establishments/" + firstEstablishment.getId() + "/departments",
            rootToken,
            "{\"name\":\"  Computer Science  \"}"
        );
        assertThat(createResponse.statusCode()).isEqualTo(200);

        JsonNode created = objectMapper.readTree(createResponse.body());
        assertThat(created.get("name").asText()).isEqualTo("Computer Science");
        assertThat(created.get("establishmentId").asText())
            .isEqualTo(firstEstablishment.getId().toString());

        HttpResponse<String> getResponse = get(
            "/api/v1/departments/" + created.get("id").asText(),
            rootToken
        );
        assertThat(getResponse.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(getResponse.body()).get("name").asText())
            .isEqualTo("Computer Science");

        HttpResponse<String> duplicateResponse = postJson(
            "/api/v1/establishments/" + firstEstablishment.getId() + "/departments",
            rootToken,
            "{\"name\":\"computer science\"}"
        );
        assertThat(duplicateResponse.statusCode()).isEqualTo(409);

        HttpResponse<String> listResponse = get(
            "/api/v1/establishments/" + firstEstablishment.getId() + "/departments",
            rootToken
        );
        assertThat(listResponse.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(listResponse.body())).hasSize(1);

        HttpResponse<String> updateResponse = patchJson(
            "/api/v1/departments/" + created.get("id").asText(),
            rootToken,
            "{\"name\":\"Software Engineering\"}"
        );
        assertThat(updateResponse.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(updateResponse.body()).get("name").asText())
            .isEqualTo("Software Engineering");

        HttpResponse<String> deleteResponse = delete(
            "/api/v1/departments/" + created.get("id").asText(),
            rootToken
        );
        assertThat(deleteResponse.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(deleteResponse.body()).get("success").asBoolean()).isTrue();
        assertThat(departmentRepository.findById(UUID.fromString(created.get("id").asText())))
            .isEmpty();
    }

    @Test
    void adminNeedsTheSpecificPermissionAndCannotCrossEstablishments() throws Exception {
        String rootToken = token(
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null,
            "root@uiz.ac.ma"
        );
        String ownDepartmentId = createDepartment(rootToken, firstEstablishment.getId(), "Computer Science");
        String otherDepartmentId = createDepartment(rootToken, secondEstablishment.getId(), "Mathematics");

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
            .findByCodeIn(java.util.Set.of(PermissionCode.DEPARTMENT_UPDATE))
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
            "/api/v1/establishments/" + firstEstablishment.getId() + "/departments",
            adminToken
        ).statusCode()).isEqualTo(403);
        assertThat(get(
            "/api/v1/departments/" + ownDepartmentId,
            adminToken
        ).statusCode()).isEqualTo(403);
        assertThat(patchJson(
            "/api/v1/departments/" + ownDepartmentId,
            adminToken,
            "{\"name\":\"Information Technology\"}"
        ).statusCode()).isEqualTo(200);
        assertThat(delete(
            "/api/v1/departments/" + ownDepartmentId,
            adminToken
        ).statusCode()).isEqualTo(403);
        assertThat(patchJson(
            "/api/v1/departments/" + otherDepartmentId,
            adminToken,
            "{\"name\":\"Applied Mathematics\"}"
        ).statusCode()).isEqualTo(403);
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

    private String createDepartment(String token, UUID establishmentId, String name) throws Exception {
        HttpResponse<String> response = postJson(
            "/api/v1/establishments/" + establishmentId + "/departments",
            token,
            "{\"name\":\"" + name + "\"}"
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

    private HttpResponse<String> patchJson(String path, String token, String body) throws Exception {
        HttpRequest request = requestBuilder(path, token)
            .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> delete(String path, String token) throws Exception {
        HttpRequest request = requestBuilder(path, token)
            .DELETE()
            .build();
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
