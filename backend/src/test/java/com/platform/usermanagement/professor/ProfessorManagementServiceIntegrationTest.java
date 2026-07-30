package com.platform.usermanagement.professor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.platform.identityaccess.application.RoleContext;
import com.platform.identityaccess.application.RoleContextService;
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
import com.platform.shared.domain.Sex;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.domain.EstablishmentStatus;
import com.platform.universitygovernance.establishment.domain.EstablishmentType;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.universitygovernance.university.domain.University;
import com.platform.universitygovernance.university.infrastructure.UniversityRepository;
import com.platform.usermanagement.professor.application.ProfessorManagementService;
import com.platform.usermanagement.professor.presentation.dto.CreateProfessorRequest;
import com.platform.usermanagement.professor.presentation.dto.CreateProfessorResponse;
import com.platform.usermanagement.professor.presentation.dto.ProfessorProfileResponse;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(classes = PlatformApplication.class)
@ActiveProfiles("test")
class ProfessorManagementServiceIntegrationTest {

    @Autowired
    private ProfessorManagementService professorManagementService;

    @Autowired
    private RoleContextService roleContextService;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AdminPermissionGrantRepository adminPermissionGrantRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RootSuperAdminRepository rootSuperAdminRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private EstablishmentRepository establishmentRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Establishment firstEstablishment;
    private Establishment secondEstablishment;

    @BeforeEach
    void setUp() {
        clearBusinessData();

        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);

        firstEstablishment = saveEstablishment(
            university,
            "ENSA Agadir",
            EstablishmentType.SCHOOL
        );
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
    void rootCanCreateAndReadProfessorWithLoginRoleContext() {
        AuthenticatedUserPrincipal root = principal(
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null
        );

        CreateProfessorResponse created = professorManagementService.createProfessor(
            root,
            firstEstablishment.getId(),
            request(" PROFESSOR@UIZ.AC.MA ", "Nadia", "Alaoui")
        );

        assertThat(created.roleType()).isEqualTo(AccountRoleType.PROFESSOR);
        assertThat(created.establishmentId()).isEqualTo(firstEstablishment.getId());
        assertThat(created.employeeNumber()).isEqualTo("EMP-1001");

        UserAccount account = userAccountRepository
            .findByUniversityEmail("professor@uiz.ac.ma")
            .orElseThrow();
        assertThat(passwordEncoder.matches("change-me-now", account.getPasswordHash()))
            .isTrue();
        assertThat(account.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);

        Professor professor = professorRepository.findByUserAccountId(account.getId())
            .orElseThrow();
        RoleContext roleContext = roleContextService.loadRoleContext(
            account,
            AccountRoleType.PROFESSOR
        );
        assertThat(roleContext.roleEntityId()).isEqualTo(professor.getId());
        assertThat(roleContext.establishmentId()).isEqualTo(firstEstablishment.getId());

        ProfessorProfileResponse profile = professorManagementService.getProfessor(
            root,
            professor.getId()
        );
        assertThat(profile.firstName()).isEqualTo("Nadia");
        assertThat(profile.birthDate()).isEqualTo(LocalDate.of(1985, 4, 12));
        assertThat(profile.employeeNumber()).isEqualTo("EMP-1001");
        assertThat(profile.academicRank()).isEqualTo("Assistant Professor");
        assertThat(profile.hireDate()).isEqualTo(LocalDate.of(2015, 9, 1));
        assertThat(profile.maximumWeeklyTeachingMinutes()).isEqualTo(480);
        assertThat(profile.placeOfBirth()).isEqualTo("Agadir");
        assertThat(profile.nationality()).isEqualTo("Moroccan");
        assertThat(profile.cin()).isEqualTo("JA123456");
        assertThat(professorManagementService.getProfessors(
            root,
            firstEstablishment.getId()
        ))
            .extracting(ProfessorProfileResponse::professorId)
            .containsExactly(professor.getId());

        assertThatThrownBy(() -> professorManagementService.createProfessor(
            root,
            firstEstablishment.getId(),
            request("professor@uiz.ac.ma", "Duplicate", "Professor")
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");
    }

    @Test
    void adminNeedsPermissionAndMatchingEstablishment() {
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

        Permission createPermission = permissionRepository
            .findByCodeIn(Set.of(PermissionCode.PROFESSOR_CREATE))
            .get(0);
        AdminPermissionGrant grant = new AdminPermissionGrant();
        grant.setAdmin(admin);
        grant.setPermission(createPermission);
        adminPermissionGrantRepository.save(grant);

        AuthenticatedUserPrincipal adminPrincipal = principal(
            AccountRoleType.ADMIN,
            admin.getId(),
            firstEstablishment.getId()
        );

        CreateProfessorResponse created = professorManagementService.createProfessor(
            adminPrincipal,
            firstEstablishment.getId(),
            request("professor2@uiz.ac.ma", "Karim", "Bennani")
        );
        assertThat(created.professorId()).isNotNull();

        assertThatThrownBy(() -> professorManagementService.getProfessor(
            adminPrincipal,
            created.professorId()
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");

        assertThatThrownBy(() -> professorManagementService.createProfessor(
            adminPrincipal,
            secondEstablishment.getId(),
            request("denied@uiz.ac.ma", "Denied", "Professor")
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
    }

    private CreateProfessorRequest request(
        String email,
        String firstName,
        String lastName
    ) {
        return new CreateProfessorRequest(
            "emp-1001",
            "Assistant Professor",
            LocalDate.of(2015, 9, 1),
            480,
            "ja123456",
            email,
            "change-me-now",
            firstName,
            lastName,
            LocalDate.of(1985, 4, 12),
            "Agadir",
            "Moroccan",
            Sex.FEMALE,
            "0612345678"
        );
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
        professorRepository.deleteAll();
        studentRepository.deleteAll();
        adminRepository.deleteAll();
        superAdminRepository.deleteAll();
        establishmentRepository.deleteAll();
        rootSuperAdminRepository.deleteAll();
        universityRepository.deleteAll();
        userProfileRepository.deleteAll();
        userAccountRepository.deleteAll();
    }
}
