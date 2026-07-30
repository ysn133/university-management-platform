package com.platform.usermanagement.student;

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
import com.platform.identityaccess.domain.Student;
import com.platform.identityaccess.domain.UserAccount;
import com.platform.identityaccess.infrastructure.AdminPermissionGrantRepository;
import com.platform.identityaccess.infrastructure.AdminRepository;
import com.platform.identityaccess.infrastructure.PermissionRepository;
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
import com.platform.usermanagement.student.application.StudentManagementService;
import com.platform.usermanagement.student.presentation.dto.CreateStudentRequest;
import com.platform.usermanagement.student.presentation.dto.CreateStudentResponse;
import com.platform.usermanagement.student.presentation.dto.StudentProfileResponse;
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
class StudentManagementServiceIntegrationTest {

    @Autowired
    private StudentManagementService studentManagementService;

    @Autowired
    private RoleContextService roleContextService;

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

        firstEstablishment = saveEstablishment(university, "ENSA Agadir", EstablishmentType.SCHOOL);
        secondEstablishment = saveEstablishment(university, "Faculty of Sciences", EstablishmentType.FACULTY);
    }

    @AfterEach
    void tearDown() {
        clearBusinessData();
    }

    @Test
    void rootCanCreateAndReadStudentWithLoginRoleContext() {
        AuthenticatedUserPrincipal root = principal(AccountRoleType.ROOT_SUPER_ADMIN, UUID.randomUUID(), null);

        CreateStudentResponse created = studentManagementService.createStudent(
            root,
            firstEstablishment.getId(),
            request(" STUDENT@UIZ.AC.MA ", "Yassine", "Amrani")
        );

        assertThat(created.roleType()).isEqualTo(AccountRoleType.STUDENT);
        assertThat(created.establishmentId()).isEqualTo(firstEstablishment.getId());
        assertThat(created.apogeeCode()).isEqualTo("APO-10001");

        UserAccount account = userAccountRepository.findByUniversityEmail("student@uiz.ac.ma")
            .orElseThrow();
        assertThat(passwordEncoder.matches("change-me-now", account.getPasswordHash())).isTrue();
        assertThat(account.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);

        Student student = studentRepository.findByUserAccountId(account.getId()).orElseThrow();
        RoleContext roleContext = roleContextService.loadRoleContext(account, AccountRoleType.STUDENT);
        assertThat(roleContext.roleEntityId()).isEqualTo(student.getId());
        assertThat(roleContext.establishmentId()).isEqualTo(firstEstablishment.getId());

        StudentProfileResponse profile = studentManagementService.getStudent(root, student.getId());
        assertThat(profile.firstName()).isEqualTo("Yassine");
        assertThat(profile.apogeeCode()).isEqualTo("APO-10001");
        assertThat(profile.nationalStudentCode()).isEqualTo("D123456789");
        assertThat(profile.initialEnrollmentDate()).isEqualTo(LocalDate.of(2023, 9, 1));
        assertThat(profile.birthDate()).isEqualTo(LocalDate.of(2002, 5, 10));
        assertThat(profile.placeOfBirth()).isEqualTo("Agadir");
        assertThat(profile.nationality()).isEqualTo("Moroccan");
        assertThat(profile.cin()).isEqualTo("JA123456");
        assertThat(studentManagementService.getStudents(root, firstEstablishment.getId()))
            .extracting(StudentProfileResponse::studentId)
            .containsExactly(student.getId());

        assertThatThrownBy(() -> studentManagementService.createStudent(
            root,
            firstEstablishment.getId(),
            request("student@uiz.ac.ma", "Duplicate", "Student")
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");

        assertThatThrownBy(() -> studentManagementService.createStudent(
            root,
            firstEstablishment.getId(),
            request("another.student@uiz.ac.ma", "Duplicate", "Apogee")
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Apogee code already exists");
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
            .findByCodeIn(Set.of(PermissionCode.STUDENT_CREATE))
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

        CreateStudentResponse created = studentManagementService.createStudent(
            adminPrincipal,
            firstEstablishment.getId(),
            request("student2@uiz.ac.ma", "Sara", "Alaoui")
        );
        assertThat(created.studentId()).isNotNull();

        assertThatThrownBy(() -> studentManagementService.getStudent(adminPrincipal, created.studentId()))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
        assertThatThrownBy(() -> studentManagementService.createStudent(
            adminPrincipal,
            secondEstablishment.getId(),
            request("denied@uiz.ac.ma", "Denied", "Student")
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
    }

    private CreateStudentRequest request(String email, String firstName, String lastName) {
        return new CreateStudentRequest(
            "APO-10001",
            "d123456789",
            "ja123456",
            LocalDate.of(2023, 9, 1),
            email,
            "change-me-now",
            firstName,
            lastName,
            LocalDate.of(2002, 5, 10),
            "Agadir",
            "Moroccan",
            Sex.MALE,
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
