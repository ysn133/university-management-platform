package com.platform.identityaccess;

import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.Admin;
import com.platform.identityaccess.domain.RefreshTokenSession;
import com.platform.identityaccess.domain.RootSuperAdmin;
import com.platform.identityaccess.domain.SuperAdmin;
import com.platform.identityaccess.domain.UserAccount;
import com.platform.identityaccess.domain.UserProfile;
import com.platform.identityaccess.infrastructure.RefreshTokenSessionStore;
import com.platform.identityaccess.infrastructure.AdminPermissionGrantRepository;
import com.platform.identityaccess.infrastructure.AdminRepository;
import com.platform.identityaccess.infrastructure.RootSuperAdminRepository;
import com.platform.identityaccess.infrastructure.SuperAdminRepository;
import com.platform.identityaccess.infrastructure.UserAccountRepository;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.PlatformApplication;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.department.infrastructure.DepartmentRepository;
import com.platform.universitygovernance.establishment.domain.EstablishmentStatus;
import com.platform.universitygovernance.establishment.domain.EstablishmentType;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.universitygovernance.university.domain.University;
import com.platform.universitygovernance.university.infrastructure.UniversityRepository;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    classes = {
        PlatformApplication.class,
        AuthControllerIntegrationTest.TestRefreshTokenSessionStoreConfiguration.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private RootSuperAdminRepository rootSuperAdminRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AdminPermissionGrantRepository adminPermissionGrantRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenSessionStore refreshTokenSessionStore;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private EstablishmentRepository establishmentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

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

        UserAccount userAccount = new UserAccount();
        userAccount.setUniversityEmail("root@uiz.ac.ma");
        userAccount.setPasswordHash(passwordEncoder.encode("change-me-now"));
        userAccount.setRole(AccountRoleType.ROOT_SUPER_ADMIN);
        userAccount.setAccountStatus(AccountStatus.ACTIVE);
        userAccountRepository.save(userAccount);

        UserProfile userProfile = new UserProfile();
        userProfile.setUserAccount(userAccount);
        userProfile.setFirstName("Root");
        userProfile.setLastName("Admin");
        userProfileRepository.save(userProfile);

        RootSuperAdmin rootSuperAdmin = new RootSuperAdmin();
        rootSuperAdmin.setUserAccount(userAccount);
        rootSuperAdminRepository.save(rootSuperAdmin);
    }

    @Test
    void loginReturnsTokensAndCurrentUser() throws Exception {
        HttpResponse<String> loginResponse = postJson(
            "/api/v1/auth/login",
            """
                {
                  "universityEmail": "root@uiz.ac.ma",
                  "password": "change-me-now"
                }
                """
        );

        assertThat(loginResponse.statusCode()).isEqualTo(200);

        JsonNode loginJson = objectMapper.readTree(loginResponse.body());
        assertThat(loginJson.get("role").asText()).isEqualTo(AccountRoleType.ROOT_SUPER_ADMIN.name());
        assertThat(loginJson.get("establishmentId").isNull()).isTrue();
        assertThat(loginJson.get("universityEmail").asText()).isEqualTo("root@uiz.ac.ma");
        assertThat(loginJson.get("accessToken").asText()).isNotBlank();
        assertThat(loginJson.get("refreshToken").asText()).isNotBlank();
        String accessToken = loginJson.get("accessToken").asText();

        HttpResponse<String> meResponse = getWithBearer("/api/v1/auth/me", accessToken);

        assertThat(meResponse.statusCode()).isEqualTo(200);
        JsonNode meJson = objectMapper.readTree(meResponse.body());
        assertThat(meJson.get("role").asText()).isEqualTo(AccountRoleType.ROOT_SUPER_ADMIN.name());
        assertThat(meJson.get("establishmentId").isNull()).isTrue();
        assertThat(meJson.get("universityEmail").asText()).isEqualTo("root@uiz.ac.ma");
        assertThat(meJson.get("firstName").asText()).isEqualTo("Root");
        assertThat(meJson.get("lastName").asText()).isEqualTo("Admin");
        assertThat(meJson.get("accountStatus").asText()).isEqualTo(AccountStatus.ACTIVE.name());
    }

    @Test
    void configuredFrontendOriginCanCallAuthenticationApi() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl() + "/api/v1/auth/login"))
            .header("Origin", "http://localhost:5173")
            .header("Access-Control-Request-Method", "POST")
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Access-Control-Allow-Origin"))
            .contains("http://localhost:5173");
        assertThat(response.headers().firstValue("Access-Control-Allow-Methods").orElse(""))
            .contains("POST");
    }

    @Test
    void superAdminCanLoginAndGetCurrentUser() throws Exception {
        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);

        Establishment establishment = new Establishment();
        establishment.setUniversity(university);
        establishment.setName("ENSA Agadir");
        establishment.setEstablishmentType(EstablishmentType.SCHOOL);
        establishment.setEstablishmentStatus(EstablishmentStatus.ACTIVE);
        establishment = establishmentRepository.save(establishment);

        UserAccount userAccount = new UserAccount();
        userAccount.setUniversityEmail("super-admin@ensa.uiz.ac.ma");
        userAccount.setPasswordHash(passwordEncoder.encode("change-me-now"));
        userAccount.setRole(AccountRoleType.SUPER_ADMIN);
        userAccount.setAccountStatus(AccountStatus.ACTIVE);
        userAccountRepository.save(userAccount);

        UserProfile userProfile = new UserProfile();
        userProfile.setUserAccount(userAccount);
        userProfile.setFirstName("Super");
        userProfile.setLastName("Admin");
        userProfileRepository.save(userProfile);

        SuperAdmin superAdmin = new SuperAdmin();
        superAdmin.setUserAccount(userAccount);
        superAdmin.setEstablishment(establishment);
        superAdminRepository.save(superAdmin);

        HttpResponse<String> loginResponse = postJson(
            "/api/v1/auth/login",
            """
                {
                  "universityEmail": "super-admin@ensa.uiz.ac.ma",
                  "password": "change-me-now"
                }
                """
        );

        assertThat(loginResponse.statusCode()).isEqualTo(200);

        JsonNode loginJson = objectMapper.readTree(loginResponse.body());
        assertThat(loginJson.get("role").asText()).isEqualTo(AccountRoleType.SUPER_ADMIN.name());
        assertThat(loginJson.get("establishmentId").asText()).isEqualTo(establishment.getId().toString());
        assertThat(loginJson.get("universityEmail").asText()).isEqualTo("super-admin@ensa.uiz.ac.ma");
        String accessToken = loginJson.get("accessToken").asText();

        HttpResponse<String> meResponse = getWithBearer("/api/v1/auth/me", accessToken);

        assertThat(meResponse.statusCode()).isEqualTo(200);

        JsonNode meJson = objectMapper.readTree(meResponse.body());
        assertThat(meJson.get("role").asText()).isEqualTo(AccountRoleType.SUPER_ADMIN.name());
        assertThat(meJson.get("roleEntityId").asText()).isEqualTo(superAdmin.getId().toString());
        assertThat(meJson.get("establishmentId").asText()).isEqualTo(establishment.getId().toString());
        assertThat(meJson.get("firstName").asText()).isEqualTo("Super");
        assertThat(meJson.get("lastName").asText()).isEqualTo("Admin");
    }

    @Test
    void adminCanLoginAndGetCurrentUser() throws Exception {
        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);

        Establishment establishment = new Establishment();
        establishment.setUniversity(university);
        establishment.setName("FSJES Agadir");
        establishment.setEstablishmentType(EstablishmentType.FACULTY);
        establishment.setEstablishmentStatus(EstablishmentStatus.ACTIVE);
        establishment = establishmentRepository.save(establishment);

        UserAccount userAccount = new UserAccount();
        userAccount.setUniversityEmail("admin@fsjes.uiz.ac.ma");
        userAccount.setPasswordHash(passwordEncoder.encode("change-me-now"));
        userAccount.setRole(AccountRoleType.ADMIN);
        userAccount.setAccountStatus(AccountStatus.ACTIVE);
        userAccountRepository.save(userAccount);

        UserProfile userProfile = new UserProfile();
        userProfile.setUserAccount(userAccount);
        userProfile.setFirstName("Local");
        userProfile.setLastName("Admin");
        userProfileRepository.save(userProfile);

        Admin admin = new Admin();
        admin.setUserAccount(userAccount);
        admin.setEstablishment(establishment);
        adminRepository.save(admin);

        HttpResponse<String> loginResponse = postJson(
            "/api/v1/auth/login",
            """
                {
                  "universityEmail": "admin@fsjes.uiz.ac.ma",
                  "password": "change-me-now"
                }
                """
        );

        assertThat(loginResponse.statusCode()).isEqualTo(200);

        JsonNode loginJson = objectMapper.readTree(loginResponse.body());
        assertThat(loginJson.get("role").asText()).isEqualTo(AccountRoleType.ADMIN.name());
        assertThat(loginJson.get("establishmentId").asText()).isEqualTo(establishment.getId().toString());

        String accessToken = loginJson.get("accessToken").asText();

        HttpResponse<String> meResponse = getWithBearer("/api/v1/auth/me", accessToken);

        assertThat(meResponse.statusCode()).isEqualTo(200);

        JsonNode meJson = objectMapper.readTree(meResponse.body());
        assertThat(meJson.get("role").asText()).isEqualTo(AccountRoleType.ADMIN.name());
        assertThat(meJson.get("roleEntityId").asText()).isEqualTo(admin.getId().toString());
        assertThat(meJson.get("establishmentId").asText()).isEqualTo(establishment.getId().toString());
        assertThat(meJson.get("firstName").asText()).isEqualTo("Local");
        assertThat(meJson.get("lastName").asText()).isEqualTo("Admin");
    }

    @Test
    void refreshRotatesTokenAndLogoutInvalidatesIt() throws Exception {
        HttpResponse<String> loginResponse = postJson(
            "/api/v1/auth/login",
            """
                {
                  "universityEmail": "root@uiz.ac.ma",
                  "password": "change-me-now"
                }
                """
        );

        assertThat(loginResponse.statusCode()).isEqualTo(200);

        JsonNode loginJson = objectMapper.readTree(loginResponse.body());
        String accessToken = loginJson.get("accessToken").asText();
        String refreshToken = loginJson.get("refreshToken").asText();

        HttpResponse<String> refreshResponse = postJson(
            "/api/v1/auth/refresh",
            """
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshToken)
        );

        assertThat(refreshResponse.statusCode()).isEqualTo(200);

        JsonNode refreshJson = objectMapper.readTree(refreshResponse.body());
        String rotatedRefreshToken = refreshJson.get("refreshToken").asText();

        assertThat(rotatedRefreshToken).isNotEqualTo(refreshToken);

        HttpResponse<String> logoutResponse = postJsonWithBearer(
            "/api/v1/auth/logout",
            accessToken,
            """
                {
                  "refreshToken": "%s"
                }
                """.formatted(rotatedRefreshToken)
        );

        assertThat(logoutResponse.statusCode()).isEqualTo(204);

        HttpResponse<String> secondRefreshResponse = postJson(
            "/api/v1/auth/refresh",
            """
                {
                  "refreshToken": "%s"
                }
                """.formatted(rotatedRefreshToken)
        );

        assertThat(secondRefreshResponse.statusCode()).isEqualTo(401);
    }

    @Test
    void authenticatedUserCanChangeOwnPassword() throws Exception {
        HttpResponse<String> loginResponse = postJson(
            "/api/v1/auth/login",
            """
                {
                  "universityEmail": "root@uiz.ac.ma",
                  "password": "change-me-now"
                }
                """
        );

        assertThat(loginResponse.statusCode()).isEqualTo(200);

        JsonNode loginJson = objectMapper.readTree(loginResponse.body());
        String accessToken = loginJson.get("accessToken").asText();

        HttpResponse<String> changePasswordResponse = postJsonWithBearer(
            "/api/v1/auth/change-password",
            accessToken,
            """
                {
                  "currentPassword": "change-me-now",
                  "newPassword": "new-password-123"
                }
                """
        );

        assertThat(changePasswordResponse.statusCode()).isEqualTo(200);

        JsonNode changePasswordJson = objectMapper.readTree(changePasswordResponse.body());
        assertThat(changePasswordJson.get("success").asBoolean()).isTrue();
        assertThat(changePasswordJson.get("message").asText()).isEqualTo("Password changed successfully");

        HttpResponse<String> oldPasswordLoginResponse = postJson(
            "/api/v1/auth/login",
            """
                {
                  "universityEmail": "root@uiz.ac.ma",
                  "password": "change-me-now"
                }
                """
        );

        assertThat(oldPasswordLoginResponse.statusCode()).isEqualTo(401);

        HttpResponse<String> newPasswordLoginResponse = postJson(
            "/api/v1/auth/login",
            """
                {
                  "universityEmail": "root@uiz.ac.ma",
                  "password": "new-password-123"
                }
                """
        );

        assertThat(newPasswordLoginResponse.statusCode()).isEqualTo(200);
    }

    @Test
    void authenticatedUserCanGetCurrentUniversity() throws Exception {
        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);

        HttpResponse<String> loginResponse = postJson(
            "/api/v1/auth/login",
            """
                {
                  "universityEmail": "root@uiz.ac.ma",
                  "password": "change-me-now"
                }
                """
        );

        assertThat(loginResponse.statusCode()).isEqualTo(200);

        JsonNode loginJson = objectMapper.readTree(loginResponse.body());
        String accessToken = loginJson.get("accessToken").asText();

        HttpResponse<String> universityResponse = getWithBearer("/api/v1/university", accessToken);

        assertThat(universityResponse.statusCode()).isEqualTo(200);

        JsonNode universityJson = objectMapper.readTree(universityResponse.body());
        assertThat(universityJson.get("universityName").asText()).isEqualTo("Universite Ibn Zohr");

        HttpResponse<String> universityByIdResponse = getWithBearer(
            "/api/v1/university/" + university.getId(),
            accessToken
        );

        assertThat(universityByIdResponse.statusCode()).isEqualTo(200);

        JsonNode universityByIdJson = objectMapper.readTree(universityByIdResponse.body());
        assertThat(universityByIdJson.get("universityId").asText()).isEqualTo(university.getId().toString());
        assertThat(universityByIdJson.get("universityName").asText()).isEqualTo("Universite Ibn Zohr");
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
