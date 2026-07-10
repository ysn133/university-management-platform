package com.platform.universitygovernance.establishment;

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
import com.platform.universitygovernance.establishment.domain.EstablishmentStatus;
import com.platform.universitygovernance.department.infrastructure.DepartmentRepository;
import com.platform.universitygovernance.establishment.domain.EstablishmentType;
import com.platform.universitygovernance.establishment.domain.Establishment;
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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    classes = {
        PlatformApplication.class,
        EstablishmentControllerIntegrationTest.TestRefreshTokenSessionStoreConfiguration.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
class EstablishmentControllerIntegrationTest {

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
    void authenticatedUserCanCreateAndReadEstablishment() throws Exception {
        String accessToken = loginAndGetAccessToken();

        HttpResponse<String> createResponse = postJsonWithBearer(
            "/api/v1/establishments",
            accessToken,
            """
                {
                  "universityId": "%s",
                  "name": "ENSA Agadir",
                  "type": "SCHOOL"
                }
                """.formatted(universityId)
        );

        assertThat(createResponse.statusCode()).isEqualTo(200);

        JsonNode createdJson = objectMapper.readTree(createResponse.body());
        String establishmentId = createdJson.get("id").asText();

        assertThat(createdJson.get("universityId").asText()).isEqualTo(universityId.toString());
        assertThat(createdJson.get("name").asText()).isEqualTo("ENSA Agadir");
        assertThat(createdJson.get("type").asText()).isEqualTo("SCHOOL");
        assertThat(createdJson.get("status").asText()).isEqualTo(EstablishmentStatus.ACTIVE.name());

        HttpResponse<String> byIdResponse = getWithBearer(
            "/api/v1/establishments/" + establishmentId,
            accessToken
        );

        assertThat(byIdResponse.statusCode()).isEqualTo(200);

        JsonNode byIdJson = objectMapper.readTree(byIdResponse.body());
        assertThat(byIdJson.get("id").asText()).isEqualTo(establishmentId);
        assertThat(byIdJson.get("name").asText()).isEqualTo("ENSA Agadir");

        HttpResponse<String> listResponse = getWithBearer(
            "/api/v1/university/" + universityId + "/establishments",
            accessToken
        );

        assertThat(listResponse.statusCode()).isEqualTo(200);

        JsonNode listJson = objectMapper.readTree(listResponse.body());
        assertThat(listJson.isArray()).isTrue();
        assertThat(listJson).hasSize(1);
        assertThat(listJson.get(0).get("id").asText()).isEqualTo(establishmentId);
    }

    @Test
    void authenticatedUserCanCreateSuperAdminForEstablishmentAndLogin() throws Exception {
        String accessToken = loginAndGetAccessToken();

        HttpResponse<String> createEstablishmentResponse = postJsonWithBearer(
            "/api/v1/establishments",
            accessToken,
            """
                {
                  "universityId": "%s",
                  "name": "ENSA Agadir",
                  "type": "SCHOOL"
                }
                """.formatted(universityId)
        );

        assertThat(createEstablishmentResponse.statusCode()).isEqualTo(200);

        JsonNode establishmentJson = objectMapper.readTree(createEstablishmentResponse.body());
        String establishmentId = establishmentJson.get("id").asText();

        HttpResponse<String> createSuperAdminResponse = postJsonWithBearer(
            "/api/v1/establishments/" + establishmentId + "/super-admins",
            accessToken,
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

        assertThat(createSuperAdminResponse.statusCode()).isEqualTo(200);

        JsonNode createdSuperAdminJson = objectMapper.readTree(createSuperAdminResponse.body());
        assertThat(createdSuperAdminJson.get("userAccountId").asText()).isNotBlank();
        assertThat(createdSuperAdminJson.get("establishmentId").asText()).isEqualTo(establishmentId);
        assertThat(createdSuperAdminJson.get("roleType").asText()).isEqualTo(AccountRoleType.SUPER_ADMIN.name());

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
        assertThat(loginJson.get("establishmentId").asText()).isEqualTo(establishmentId);

        String superAdminAccessToken = loginJson.get("accessToken").asText();

        HttpResponse<String> meResponse = getWithBearer("/api/v1/auth/me", superAdminAccessToken);

        assertThat(meResponse.statusCode()).isEqualTo(200);

        JsonNode meJson = objectMapper.readTree(meResponse.body());
        assertThat(meJson.get("role").asText()).isEqualTo(AccountRoleType.SUPER_ADMIN.name());
        assertThat(meJson.get("establishmentId").asText()).isEqualTo(establishmentId);
        assertThat(meJson.get("firstName").asText()).isEqualTo("Super");
        assertThat(meJson.get("lastName").asText()).isEqualTo("Admin");

        UserAccount createdUserAccount = userAccountRepository.findByUniversityEmail("super-admin@ensa.uiz.ac.ma")
            .orElseThrow();
        assertThat(passwordEncoder.matches("change-me-now", createdUserAccount.getPasswordHash())).isTrue();

        UserProfile createdUserProfile = userProfileRepository.findByUserAccountId(createdUserAccount.getId())
            .orElseThrow();
        assertThat(createdUserProfile.getFirstName()).isEqualTo("Super");
        assertThat(createdUserProfile.getLastName()).isEqualTo("Admin");

        SuperAdmin createdSuperAdmin = superAdminRepository.findByUserAccountId(createdUserAccount.getId())
            .orElseThrow();
        assertThat(createdSuperAdmin.getEstablishment().getId()).isEqualTo(UUID.fromString(establishmentId));
    }

    @Test
    void rootCanGetSuperAdminAndListOnlyTheEstablishmentSuperAdmins() throws Exception {
        String accessToken = loginAndGetAccessToken();
        Establishment firstEstablishment = createEstablishment("ENSA Agadir", EstablishmentType.SCHOOL);
        Establishment secondEstablishment = createEstablishment("Faculty of Sciences", EstablishmentType.FACULTY);

        SuperAdmin firstSuperAdmin = createSuperAdmin(
            firstEstablishment,
            "super-admin@ensa.uiz.ac.ma",
            "First",
            "Manager"
        );
        createSuperAdmin(
            secondEstablishment,
            "super-admin@fsa.uiz.ac.ma",
            "Second",
            "Manager"
        );

        HttpResponse<String> getResponse = getWithBearer(
            "/api/v1/super-admins/" + firstSuperAdmin.getId(),
            accessToken
        );

        assertThat(getResponse.statusCode()).isEqualTo(200);
        JsonNode getJson = objectMapper.readTree(getResponse.body());
        assertThat(getJson.get("id").asText()).isEqualTo(firstSuperAdmin.getId().toString());
        assertThat(getJson.get("accountId").asText())
            .isEqualTo(firstSuperAdmin.getUserAccount().getId().toString());
        assertThat(getJson.get("establishmentId").asText()).isEqualTo(firstEstablishment.getId().toString());
        assertThat(getJson.get("email").asText()).isEqualTo("super-admin@ensa.uiz.ac.ma");
        assertThat(getJson.get("firstName").asText()).isEqualTo("First");

        HttpResponse<String> listResponse = getWithBearer(
            "/api/v1/establishments/" + firstEstablishment.getId() + "/super-admins",
            accessToken
        );

        assertThat(listResponse.statusCode()).isEqualTo(200);
        JsonNode listJson = objectMapper.readTree(listResponse.body());
        assertThat(listJson.isArray()).isTrue();
        assertThat(listJson).hasSize(1);
        assertThat(listJson.get(0).get("id").asText()).isEqualTo(firstSuperAdmin.getId().toString());
    }

    @Test
    void superAdminReadEndpointsReturnNotFoundForUnknownResources() throws Exception {
        String accessToken = loginAndGetAccessToken();

        HttpResponse<String> missingSuperAdminResponse = getWithBearer(
            "/api/v1/super-admins/" + UUID.randomUUID(),
            accessToken
        );
        HttpResponse<String> missingEstablishmentResponse = getWithBearer(
            "/api/v1/establishments/" + UUID.randomUUID() + "/super-admins",
            accessToken
        );

        assertThat(missingSuperAdminResponse.statusCode()).isEqualTo(404);
        assertThat(missingEstablishmentResponse.statusCode()).isEqualTo(404);
    }

    @Test
    void nonRootUserCannotReadSuperAdminManagementEndpoints() throws Exception {
        Establishment establishment = createEstablishment("ENSA Agadir", EstablishmentType.SCHOOL);
        SuperAdmin superAdmin = createSuperAdmin(
            establishment,
            "super-admin@ensa.uiz.ac.ma",
            "Super",
            "Admin"
        );
        String superAdminAccessToken = loginAndGetAccessToken(
            "super-admin@ensa.uiz.ac.ma",
            "change-me-now"
        );

        HttpResponse<String> getResponse = getWithBearer(
            "/api/v1/super-admins/" + superAdmin.getId(),
            superAdminAccessToken
        );
        HttpResponse<String> listResponse = getWithBearer(
            "/api/v1/establishments/" + establishment.getId() + "/super-admins",
            superAdminAccessToken
        );

        assertThat(getResponse.statusCode()).isEqualTo(403);
        assertThat(listResponse.statusCode()).isEqualTo(403);
    }

    private Establishment createEstablishment(String name, EstablishmentType type) {
        Establishment establishment = new Establishment();
        establishment.setUniversity(universityRepository.findById(universityId).orElseThrow());
        establishment.setName(name);
        establishment.setEstablishmentType(type);
        establishment.setEstablishmentStatus(EstablishmentStatus.ACTIVE);
        return establishmentRepository.save(establishment);
    }

    private SuperAdmin createSuperAdmin(
        Establishment establishment,
        String universityEmail,
        String firstName,
        String lastName
    ) {
        UserAccount account = new UserAccount();
        account.setUniversityEmail(universityEmail);
        account.setPasswordHash(passwordEncoder.encode("change-me-now"));
        account.setRole(AccountRoleType.SUPER_ADMIN);
        account.setAccountStatus(AccountStatus.ACTIVE);
        userAccountRepository.save(account);

        UserProfile profile = new UserProfile();
        profile.setUserAccount(account);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        userProfileRepository.save(profile);

        SuperAdmin superAdmin = new SuperAdmin();
        superAdmin.setUserAccount(account);
        superAdmin.setEstablishment(establishment);
        return superAdminRepository.save(superAdmin);
    }

    private String loginAndGetAccessToken() throws Exception {
        return loginAndGetAccessToken("root@uiz.ac.ma", "change-me-now");
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

        JsonNode loginJson = objectMapper.readTree(loginResponse.body());
        return loginJson.get("accessToken").asText();
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
