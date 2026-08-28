package com.platform.usermanagement.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.Admin;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.RefreshTokenSession;
import com.platform.identityaccess.domain.RootSuperAdmin;
import com.platform.identityaccess.domain.SuperAdmin;
import com.platform.identityaccess.domain.UserAccount;
import com.platform.identityaccess.domain.UserProfile;
import com.platform.identityaccess.infrastructure.AdminRepository;
import com.platform.identityaccess.infrastructure.AdminPermissionGrantRepository;
import com.platform.identityaccess.infrastructure.RefreshTokenSessionStore;
import com.platform.identityaccess.infrastructure.RootSuperAdminRepository;
import com.platform.identityaccess.infrastructure.SuperAdminRepository;
import com.platform.identityaccess.infrastructure.UserAccountRepository;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.PlatformApplication;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.department.infrastructure.DepartmentRepository;
import com.platform.universitygovernance.establishment.domain.EstablishmentStatus;
import com.platform.universitygovernance.establishment.domain.EstablishmentType;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.universitygovernance.university.domain.University;
import com.platform.universitygovernance.university.infrastructure.UniversityRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(
    classes = {
        PlatformApplication.class,
        AdminManagementControllerIntegrationTest.TestRefreshTokenSessionStoreConfiguration.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
class AdminManagementControllerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private RootSuperAdminRepository rootSuperAdminRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AdminPermissionGrantRepository adminPermissionGrantRepository;

    @Autowired
    private AdminPermissionAuthorizationService adminPermissionAuthorizationService;

    @Autowired
    private RefreshTokenSessionStore refreshTokenSessionStore;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private EstablishmentRepository establishmentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private UUID universityId;

    @BeforeEach
    void setUp() {
        ((InMemoryRefreshTokenSessionStore) refreshTokenSessionStore).clear();
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
        universityId = university.getId();

        UserAccount rootAccount = new UserAccount();
        rootAccount.setUniversityEmail("root@uiz.ac.ma");
        rootAccount.setPasswordHash(passwordEncoder.encode("change-me-now"));
        rootAccount.setRole(AccountRoleType.ROOT_SUPER_ADMIN);
        rootAccount.setAccountStatus(AccountStatus.ACTIVE);
        userAccountRepository.save(rootAccount);

        UserProfile rootProfile = new UserProfile();
        rootProfile.setUserAccount(rootAccount);
        rootProfile.setFirstName("Root");
        rootProfile.setLastName("Admin");
        userProfileRepository.save(rootProfile);

        RootSuperAdmin rootSuperAdmin = new RootSuperAdmin();
        rootSuperAdmin.setUserAccount(rootAccount);
        rootSuperAdminRepository.save(rootSuperAdmin);
    }

    @Test
    void rootCanCreateAdminForEstablishment() throws Exception {
        String rootAccessToken = loginAndGetAccessToken("root@uiz.ac.ma", "change-me-now");
        String establishmentId = createEstablishment(rootAccessToken);

        HttpResponse<String> createAdminResponse = postJsonWithBearer(
            "/api/v1/establishments/" + establishmentId + "/admins",
            rootAccessToken,
            """
                {
                  "universityEmail": "admin1@ensa.uiz.ac.ma",
                  "password": "change-me-now",
                  "firstName": "Main",
                  "lastName": "Admin",
                  "birth_date": "1992-04-10",
                  "sex": "FEMALE",
                  "phone_number": "0611111111"
                }
                """
        );

        assertThat(createAdminResponse.statusCode()).isEqualTo(200);

        JsonNode createAdminJson = objectMapper.readTree(createAdminResponse.body());
        assertThat(createAdminJson.get("roleType").asText()).isEqualTo(AccountRoleType.ADMIN.name());
        assertThat(createAdminJson.get("establishmentId").asText()).isEqualTo(establishmentId);
        assertThat(createAdminJson.get("adminId").asText()).isNotBlank();

        UserAccount createdUserAccount = userAccountRepository.findByUniversityEmail("admin1@ensa.uiz.ac.ma")
            .orElseThrow();
        assertThat(passwordEncoder.matches("change-me-now", createdUserAccount.getPasswordHash())).isTrue();

        Admin createdAdmin = adminRepository.findByUserAccountId(createdUserAccount.getId())
            .orElseThrow();
        assertThat(createdAdmin.getEstablishment().getId()).isEqualTo(UUID.fromString(establishmentId));
    }

    @Test
    void superAdminCanCreateAdminOnlyInsideOwnEstablishment() throws Exception {
        String rootAccessToken = loginAndGetAccessToken("root@uiz.ac.ma", "change-me-now");
        String establishmentId = createEstablishment(rootAccessToken);
        createSuperAdmin(rootAccessToken, establishmentId);

        String superAdminAccessToken = loginAndGetAccessToken("super-admin@ensa.uiz.ac.ma", "change-me-now");

        HttpResponse<String> createAdminResponse = postJsonWithBearer(
            "/api/v1/establishments/" + establishmentId + "/admins",
            superAdminAccessToken,
            """
                {
                  "universityEmail": "admin2@ensa.uiz.ac.ma",
                  "password": "change-me-now",
                  "firstName": "Branch",
                  "lastName": "Admin",
                  "birth_date": "1991-03-12",
                  "sex": "MALE",
                  "phone_number": "0622222222"
                }
                """
        );

        assertThat(createAdminResponse.statusCode()).isEqualTo(200);

        HttpResponse<String> loginResponse = postJson(
            "/api/v1/auth/login",
            """
                {
                  "universityEmail": "admin2@ensa.uiz.ac.ma",
                  "password": "change-me-now"
                }
                """
        );

        assertThat(loginResponse.statusCode()).isEqualTo(200);

        JsonNode loginJson = objectMapper.readTree(loginResponse.body());
        assertThat(loginJson.get("role").asText()).isEqualTo(AccountRoleType.ADMIN.name());
        assertThat(loginJson.get("establishmentId").asText()).isEqualTo(establishmentId);

        String otherEstablishmentId = createEstablishment(
            rootAccessToken,
            """
                {
                  "universityId": "%s",
                  "name": "FSJES Agadir",
                  "type": "FACULTY"
                }
                """.formatted(universityId)
        );

        HttpResponse<String> forbiddenResponse = postJsonWithBearer(
            "/api/v1/establishments/" + otherEstablishmentId + "/admins",
            superAdminAccessToken,
            """
                {
                  "universityEmail": "admin3@fsjes.uiz.ac.ma",
                  "password": "change-me-now",
                  "firstName": "Denied",
                  "lastName": "Admin",
                  "birth_date": "1990-01-01",
                  "sex": "MALE",
                  "phone_number": "0633333333"
                }
                """
        );

        assertThat(forbiddenResponse.statusCode()).isEqualTo(403);
    }

    @Test
    void rootCanGetAdminAndListOnlyAdminsFromTheSelectedEstablishment() throws Exception {
        String rootAccessToken = loginAndGetAccessToken("root@uiz.ac.ma", "change-me-now");
        String firstEstablishmentId = createEstablishment(rootAccessToken);
        String secondEstablishmentId = createEstablishment(
            rootAccessToken,
            """
                {
                  "universityId": "%s",
                  "name": "Faculty of Sciences",
                  "type": "FACULTY"
                }
                """.formatted(universityId)
        );

        String firstAdminId = createAdmin(
            rootAccessToken,
            firstEstablishmentId,
            "first-admin@ensa.uiz.ac.ma",
            "First"
        );
        createAdmin(
            rootAccessToken,
            secondEstablishmentId,
            "second-admin@fsa.uiz.ac.ma",
            "Second"
        );

        HttpResponse<String> getResponse = getWithBearer(
            "/api/v1/admins/" + firstAdminId,
            rootAccessToken
        );

        assertThat(getResponse.statusCode()).isEqualTo(200);
        JsonNode getJson = objectMapper.readTree(getResponse.body());
        assertThat(getJson.get("id").asText()).isEqualTo(firstAdminId);
        assertThat(getJson.get("establishmentId").asText()).isEqualTo(firstEstablishmentId);
        assertThat(getJson.get("email").asText()).isEqualTo("first-admin@ensa.uiz.ac.ma");
        assertThat(getJson.get("firstName").asText()).isEqualTo("First");
        assertThat(getJson.get("role").asText()).isEqualTo(AccountRoleType.ADMIN.name());

        HttpResponse<String> listResponse = getWithBearer(
            "/api/v1/establishments/" + firstEstablishmentId + "/admins",
            rootAccessToken
        );

        assertThat(listResponse.statusCode()).isEqualTo(200);
        JsonNode listJson = objectMapper.readTree(listResponse.body());
        assertThat(listJson.isArray()).isTrue();
        assertThat(listJson).hasSize(1);
        assertThat(listJson.get(0).get("id").asText()).isEqualTo(firstAdminId);
    }

    @Test
    void superAdminCanManageOnlyAdminsFromOwnEstablishment() throws Exception {
        String rootAccessToken = loginAndGetAccessToken("root@uiz.ac.ma", "change-me-now");
        String ownEstablishmentId = createEstablishment(rootAccessToken);
        String otherEstablishmentId = createEstablishment(
            rootAccessToken,
            """
                {
                  "universityId": "%s",
                  "name": "FSJES Agadir",
                  "type": "FACULTY"
                }
                """.formatted(universityId)
        );
        createSuperAdmin(rootAccessToken, ownEstablishmentId);

        String ownAdminId = createAdmin(
            rootAccessToken,
            ownEstablishmentId,
            "own-admin@ensa.uiz.ac.ma",
            "Own"
        );
        String otherAdminId = createAdmin(
            rootAccessToken,
            otherEstablishmentId,
            "other-admin@fsjes.uiz.ac.ma",
            "Other"
        );
        String superAdminAccessToken = loginAndGetAccessToken(
            "super-admin@ensa.uiz.ac.ma",
            "change-me-now"
        );

        assertThat(getWithBearer(
            "/api/v1/admins/" + ownAdminId,
            superAdminAccessToken
        ).statusCode()).isEqualTo(200);
        assertThat(getWithBearer(
            "/api/v1/establishments/" + ownEstablishmentId + "/admins",
            superAdminAccessToken
        ).statusCode()).isEqualTo(200);

        assertThat(getWithBearer(
            "/api/v1/admins/" + otherAdminId,
            superAdminAccessToken
        ).statusCode()).isEqualTo(403);
        assertThat(getWithBearer(
            "/api/v1/establishments/" + otherEstablishmentId + "/admins",
            superAdminAccessToken
        ).statusCode()).isEqualTo(403);
    }

    @Test
    void rootCanResetPasswordAndManageAdminAccountLifecycle() throws Exception {
        String rootAccessToken = loginAndGetAccessToken("root@uiz.ac.ma", "change-me-now");
        String establishmentId = createEstablishment(rootAccessToken);
        String adminId = createAdmin(
            rootAccessToken,
            establishmentId,
            "lifecycle-admin@ensa.uiz.ac.ma",
            "Lifecycle"
        );

        HttpResponse<String> resetResponse = postJsonWithBearer(
            "/api/v1/admins/" + adminId + "/password-reset",
            rootAccessToken,
            """
                {
                  "newPassword": "new-password"
                }
                """
        );
        assertThat(resetResponse.statusCode()).isEqualTo(200);
        assertThat(loginAndGetAccessToken(
            "lifecycle-admin@ensa.uiz.ac.ma",
            "new-password"
        )).isNotBlank();

        assertThat(postJsonWithBearer(
            "/api/v1/admins/" + adminId + "/lock",
            rootAccessToken,
            ""
        ).statusCode()).isEqualTo(200);
        assertAdminStatus(adminId, AccountStatus.LOCKED);

        assertThat(postJsonWithBearer(
            "/api/v1/admins/" + adminId + "/unlock",
            rootAccessToken,
            ""
        ).statusCode()).isEqualTo(200);
        assertAdminStatus(adminId, AccountStatus.ACTIVE);

        assertThat(postJsonWithBearer(
            "/api/v1/admins/" + adminId + "/deactivate",
            rootAccessToken,
            ""
        ).statusCode()).isEqualTo(200);
        assertAdminStatus(adminId, AccountStatus.DEACTIVATED);

        assertThat(postJsonWithBearer(
            "/api/v1/admins/" + adminId + "/unlock",
            rootAccessToken,
            ""
        ).statusCode()).isEqualTo(409);

        assertThat(postJsonWithBearer(
            "/api/v1/admins/" + adminId + "/activate",
            rootAccessToken,
            ""
        ).statusCode()).isEqualTo(200);
        assertAdminStatus(adminId, AccountStatus.ACTIVE);

        assertThat(postJsonWithBearer(
            "/api/v1/admins/" + adminId + "/archive",
            rootAccessToken,
            ""
        ).statusCode()).isEqualTo(200);
        assertAdminStatus(adminId, AccountStatus.ARCHIVED);

        assertThat(postJsonWithBearer(
            "/api/v1/admins/" + adminId + "/restore",
            rootAccessToken,
            ""
        ).statusCode()).isEqualTo(200);
        assertAdminStatus(adminId, AccountStatus.DEACTIVATED);
    }

    @Test
    void adminManagementReadEndpointsReturnNotFoundForUnknownResources() throws Exception {
        String rootAccessToken = loginAndGetAccessToken("root@uiz.ac.ma", "change-me-now");

        HttpResponse<String> missingAdminResponse = getWithBearer(
            "/api/v1/admins/" + UUID.randomUUID(),
            rootAccessToken
        );
        HttpResponse<String> missingEstablishmentResponse = getWithBearer(
            "/api/v1/establishments/" + UUID.randomUUID() + "/admins",
            rootAccessToken
        );

        assertThat(missingAdminResponse.statusCode()).isEqualTo(404);
        assertThat(missingEstablishmentResponse.statusCode()).isEqualTo(404);
    }

    @Test
    void rootCanReplaceAdminOperationalPermissionGrants() throws Exception {
        String rootAccessToken = loginAndGetAccessToken("root@uiz.ac.ma", "change-me-now");
        String establishmentId = createEstablishment(rootAccessToken);
        String adminId = createAdmin(
            rootAccessToken,
            establishmentId,
            "permission-admin@ensa.uiz.ac.ma",
            "Permission"
        );

        HttpResponse<String> catalogResponse = getWithBearer("/api/v1/permissions", rootAccessToken);
        assertThat(catalogResponse.statusCode()).isEqualTo(200);
        JsonNode catalog = objectMapper.readTree(catalogResponse.body());
        assertThat(catalog).hasSize(PermissionCode.values().length);
        assertThat(catalog.toString()).contains(
            "ADMIN_CREATE",
            "DEPARTMENT_CREATE",
            "STUDENT_VIEW"
        );

        HttpResponse<String> replaceResponse = putJsonWithBearer(
            "/api/v1/admins/" + adminId + "/permission-grants",
            rootAccessToken,
            """
                {
                  "permissions": ["DEPARTMENT_CREATE", "STUDENT_VIEW"]
                }
                """
        );

        assertThat(replaceResponse.statusCode()).isEqualTo(200);
        JsonNode grants = objectMapper.readTree(replaceResponse.body());
        assertThat(grants.get("permissions").toString())
            .contains("DEPARTMENT_CREATE", "STUDENT_VIEW");

        Admin admin = adminRepository.findById(UUID.fromString(adminId)).orElseThrow();
        AuthenticatedUserPrincipal adminPrincipal = new AuthenticatedUserPrincipal(
            admin.getUserAccount().getId(),
            AccountRoleType.ADMIN,
            admin.getId(),
            UUID.fromString(establishmentId),
            admin.getUserAccount().getUniversityEmail()
        );

        adminPermissionAuthorizationService.requirePermission(
            adminPrincipal,
            UUID.fromString(establishmentId),
            PermissionCode.DEPARTMENT_CREATE
        );
        assertThatThrownBy(() -> adminPermissionAuthorizationService.requirePermission(
            adminPrincipal,
            UUID.fromString(establishmentId),
            PermissionCode.DEPARTMENT_VIEW
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");

        HttpResponse<String> secondReplaceResponse = putJsonWithBearer(
            "/api/v1/admins/" + adminId + "/permission-grants",
            rootAccessToken,
            """
                {
                  "permissions": ["STUDENT_VIEW", "PROFESSOR_VIEW"]
                }
                """
        );
        assertThat(secondReplaceResponse.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(secondReplaceResponse.body()).get("permissions").toString())
            .contains("STUDENT_VIEW", "PROFESSOR_VIEW")
            .doesNotContain("DEPARTMENT_CREATE");
    }

    @Test
    void superAdminManagesGrantsOnlyInsideOwnEstablishmentAndAdminCannotManageGrants() throws Exception {
        String rootAccessToken = loginAndGetAccessToken("root@uiz.ac.ma", "change-me-now");
        String ownEstablishmentId = createEstablishment(rootAccessToken);
        String otherEstablishmentId = createEstablishment(
            rootAccessToken,
            """
                {
                  "universityId": "%s",
                  "name": "Faculty of Sciences",
                  "type": "FACULTY"
                }
                """.formatted(universityId)
        );
        createSuperAdmin(rootAccessToken, ownEstablishmentId);
        String ownAdminId = createAdmin(
            rootAccessToken,
            ownEstablishmentId,
            "own-permission-admin@ensa.uiz.ac.ma",
            "Own"
        );
        String otherAdminId = createAdmin(
            rootAccessToken,
            otherEstablishmentId,
            "other-permission-admin@fsa.uiz.ac.ma",
            "Other"
        );
        String superAdminAccessToken = loginAndGetAccessToken(
            "super-admin@ensa.uiz.ac.ma",
            "change-me-now"
        );

        assertThat(putJsonWithBearer(
            "/api/v1/admins/" + ownAdminId + "/permission-grants",
            superAdminAccessToken,
            "{\"permissions\":[\"CLASS_GROUP_VIEW\"]}"
        ).statusCode()).isEqualTo(200);
        assertThat(putJsonWithBearer(
            "/api/v1/admins/" + otherAdminId + "/permission-grants",
            superAdminAccessToken,
            "{\"permissions\":[\"CLASS_GROUP_VIEW\"]}"
        ).statusCode()).isEqualTo(403);

        String adminAccessToken = loginAndGetAccessToken(
            "own-permission-admin@ensa.uiz.ac.ma",
            "change-me-now"
        );
        assertThat(getWithBearer("/api/v1/permissions", adminAccessToken).statusCode())
            .isEqualTo(403);
    }

    @Test
    void adminWithAdminCreateGrantCanCreateOnlyInsideOwnEstablishment() throws Exception {
        String rootAccessToken = loginAndGetAccessToken("root@uiz.ac.ma", "change-me-now");
        String ownEstablishmentId = createEstablishment(rootAccessToken);
        String otherEstablishmentId = createEstablishment(
            rootAccessToken,
            """
                {
                  "universityId": "%s",
                  "name": "Faculty of Sciences",
                  "type": "FACULTY"
                }
                """.formatted(universityId)
        );
        String delegatedAdminId = createAdmin(
            rootAccessToken,
            ownEstablishmentId,
            "delegated-admin@ensa.uiz.ac.ma",
            "Delegated"
        );
        String delegatedAdminToken = loginAndGetAccessToken(
            "delegated-admin@ensa.uiz.ac.ma",
            "change-me-now"
        );

        assertThat(createAdminResponse(
            delegatedAdminToken,
            ownEstablishmentId,
            "denied-before-grant@ensa.uiz.ac.ma"
        ).statusCode()).isEqualTo(403);

        assertThat(putJsonWithBearer(
            "/api/v1/admins/" + delegatedAdminId + "/permission-grants",
            rootAccessToken,
            "{\"permissions\":[\"ADMIN_CREATE\"]}"
        ).statusCode()).isEqualTo(200);

        HttpResponse<String> ownCreation = createAdminResponse(
            delegatedAdminToken,
            ownEstablishmentId,
            "created-by-admin@ensa.uiz.ac.ma"
        );
        assertThat(ownCreation.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(ownCreation.body()).get("establishmentId").asText())
            .isEqualTo(ownEstablishmentId);

        assertThat(createAdminResponse(
            delegatedAdminToken,
            otherEstablishmentId,
            "cross-establishment@fsa.uiz.ac.ma"
        ).statusCode()).isEqualTo(403);
        assertThat(getWithBearer(
            "/api/v1/establishments/" + ownEstablishmentId,
            delegatedAdminToken
        ).statusCode()).isEqualTo(200);
        assertThat(getWithBearer(
            "/api/v1/establishments/" + otherEstablishmentId,
            delegatedAdminToken
        ).statusCode()).isEqualTo(403);
        assertThat(getWithBearer(
            "/api/v1/establishments/" + ownEstablishmentId + "/admins",
            delegatedAdminToken
        ).statusCode()).isEqualTo(403);
    }

    private HttpResponse<String> createAdminResponse(
        String accessToken,
        String establishmentId,
        String universityEmail
    ) throws Exception {
        return postJsonWithBearer(
            "/api/v1/establishments/" + establishmentId + "/admins",
            accessToken,
            """
                {
                  "universityEmail": "%s",
                  "password": "change-me-now",
                  "firstName": "Created",
                  "lastName": "Admin",
                  "birth_date": "1990-01-01",
                  "sex": "MALE",
                  "phone_number": "0600000000"
                }
                """.formatted(universityEmail)
        );
    }

    private String createAdmin(
        String accessToken,
        String establishmentId,
        String universityEmail,
        String firstName
    ) throws Exception {
        HttpResponse<String> response = postJsonWithBearer(
            "/api/v1/establishments/" + establishmentId + "/admins",
            accessToken,
            """
                {
                  "universityEmail": "%s",
                  "password": "change-me-now",
                  "firstName": "%s",
                  "lastName": "Admin",
                  "birth_date": "1990-01-01",
                  "sex": "MALE",
                  "phone_number": "0600000000"
                }
                """.formatted(universityEmail, firstName)
        );

        assertThat(response.statusCode()).isEqualTo(200);
        return objectMapper.readTree(response.body()).get("adminId").asText();
    }

    private void assertAdminStatus(String adminId, AccountStatus expectedStatus) {
        Admin admin = adminRepository.findById(UUID.fromString(adminId)).orElseThrow();
        assertThat(admin.getUserAccount().getAccountStatus()).isEqualTo(expectedStatus);
    }

    private void createSuperAdmin(String rootAccessToken, String establishmentId) throws Exception {
        HttpResponse<String> response = postJsonWithBearer(
            "/api/v1/establishments/" + establishmentId + "/super-admins",
            rootAccessToken,
            """
                {
                  "universityEmail": "super-admin@ensa.uiz.ac.ma",
                  "password": "change-me-now",
                  "firstName": "Super",
                  "lastName": "Admin",
                  "birth_date": "1990-06-15",
                  "sex": "MALE",
                  "phone_number": "0600000000"
                }
                """
        );

        assertThat(response.statusCode()).isEqualTo(200);
    }

    private String createEstablishment(String accessToken) throws Exception {
        return createEstablishment(
            accessToken,
            """
                {
                  "universityId": "%s",
                  "name": "ENSA Agadir",
                  "type": "SCHOOL"
                }
                """.formatted(universityId)
        );
    }

    private String createEstablishment(String accessToken, String body) throws Exception {
        HttpResponse<String> response = postJsonWithBearer("/api/v1/establishments", accessToken, body);
        assertThat(response.statusCode()).isEqualTo(200);
        return objectMapper.readTree(response.body()).get("id").asText();
    }

    private String loginAndGetAccessToken(String universityEmail, String password) throws Exception {
        HttpResponse<String> loginResponse = postJson(
            "/api/v1/auth/login",
            """
                {
                  "universityEmail": "%s",
                  "password": "%s"
                }
                """.formatted(universityEmail, password)
        );

        assertThat(loginResponse.statusCode()).isEqualTo(200);
        return objectMapper.readTree(loginResponse.body()).get("accessToken").asText();
    }

    private HttpResponse<String> postJson(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl() + path))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postJsonWithBearer(String path, String accessToken, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl() + path))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + accessToken)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getWithBearer(String path, String accessToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl() + path))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> putJsonWithBearer(String path, String accessToken, String body)
        throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl() + path))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer " + accessToken)
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @TestConfiguration
    static class TestRefreshTokenSessionStoreConfiguration {

        @Bean
        @Primary
        InMemoryRefreshTokenSessionStore refreshTokenSessionStore() {
            return new InMemoryRefreshTokenSessionStore();
        }
    }

    static class InMemoryRefreshTokenSessionStore implements RefreshTokenSessionStore {

        private final Map<String, RefreshTokenSession> sessions = new ConcurrentHashMap<>();

        void clear() {
            sessions.clear();
        }

        @Override
        public void save(RefreshTokenSession session, Duration ttl) {
            sessions.put(session.getTokenValue(), copyOf(session));
        }

        @Override
        public Optional<RefreshTokenSession> findByToken(String tokenValue) {
            return Optional.ofNullable(sessions.get(tokenValue)).map(this::copyOf);
        }

        @Override
        public void delete(String tokenValue) {
            sessions.remove(tokenValue);
        }

        private RefreshTokenSession copyOf(RefreshTokenSession source) {
            RefreshTokenSession copy = new RefreshTokenSession();
            copy.setTokenValue(source.getTokenValue());
            copy.setUserAccountId(source.getUserAccountId());
            copy.setRole(source.getRole());
            copy.setRoleEntityId(source.getRoleEntityId());
            copy.setEstablishmentId(source.getEstablishmentId());
            copy.setUniversityEmail(source.getUniversityEmail());
            copy.setExpiresAt(source.getExpiresAt());
            return copy;
        }
    }
}
