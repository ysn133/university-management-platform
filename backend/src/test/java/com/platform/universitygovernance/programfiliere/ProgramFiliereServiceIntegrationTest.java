package com.platform.universitygovernance.programfiliere;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.degreecycle.domain.DegreeCycle;
import com.platform.universitygovernance.degreecycle.infrastructure.DegreeCycleRepository;
import com.platform.universitygovernance.department.domain.Department;
import com.platform.universitygovernance.department.infrastructure.DepartmentRepository;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.domain.EstablishmentStatus;
import com.platform.universitygovernance.establishment.domain.EstablishmentType;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.universitygovernance.programfiliere.application.ProgramFiliereService;
import com.platform.universitygovernance.programfiliere.infrastructure.ProgramFiliereRepository;
import com.platform.universitygovernance.programfiliere.presentation.dto.CreateProgramFiliereRequest;
import com.platform.universitygovernance.programfiliere.presentation.dto.ProgramFiliereResponse;
import com.platform.universitygovernance.programfiliere.presentation.dto.UpdateProgramFiliereRequest;
import com.platform.universitygovernance.programpath.domain.ProgramPath;
import com.platform.universitygovernance.programpath.infrastructure.ProgramPathRepository;
import com.platform.universitygovernance.university.domain.University;
import com.platform.universitygovernance.university.infrastructure.UniversityRepository;
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
class ProgramFiliereServiceIntegrationTest {

    @Autowired
    private ProgramFiliereService programFiliereService;

    @Autowired
    private ProgramFiliereRepository programFiliereRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DegreeCycleRepository degreeCycleRepository;

    @Autowired
    private ProgramPathRepository programPathRepository;

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

    private Establishment firstEstablishment;
    private Establishment secondEstablishment;
    private Department firstDepartment;
    private DegreeCycle firstDegreeCycle;
    private ProgramPath firstProgramPath;
    private DegreeCycle secondDegreeCycle;
    private ProgramPath secondProgramPath;

    @BeforeEach
    void setUp() {
        clearBusinessData();

        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);

        firstEstablishment = saveEstablishment(university, "ENSA Agadir", EstablishmentType.SCHOOL);
        secondEstablishment = saveEstablishment(university, "Faculty of Sciences", EstablishmentType.FACULTY);
        firstDepartment = saveDepartment(firstEstablishment, "Computer Science");
        firstDegreeCycle = saveDegreeCycle(firstEstablishment, "Master");
        firstProgramPath = saveProgramPath(firstEstablishment, "Excellence Program");
        secondDegreeCycle = saveDegreeCycle(secondEstablishment, "Licence");
        secondProgramPath = saveProgramPath(secondEstablishment, "Regular Program");
    }

    @AfterEach
    void tearDown() {
        clearBusinessData();
    }

    @Test
    void rootCanManageProgramFilieresAndInvalidParentCombinationsAreRejected() {
        AuthenticatedUserPrincipal root = principal(
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null
        );

        ProgramFiliereResponse created = programFiliereService.createProgramFiliere(
            root,
            firstDepartment.getId(),
            createRequest("  il  ", "  Software Engineering  ", firstDegreeCycle, firstProgramPath)
        );
        assertThat(created.code()).isEqualTo("IL");
        assertThat(created.name()).isEqualTo("Software Engineering");
        assertThat(programFiliereService.getProgramFilieres(root, firstDepartment.getId()))
            .extracting(ProgramFiliereResponse::id)
            .containsExactly(created.id());
        assertThat(programFiliereService.getProgramFiliere(root, created.id()).id())
            .isEqualTo(created.id());

        assertThatThrownBy(() -> programFiliereService.createProgramFiliere(
            root,
            firstDepartment.getId(),
            createRequest("il", "Duplicate", firstDegreeCycle, firstProgramPath)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");

        assertThatThrownBy(() -> programFiliereService.createProgramFiliere(
            root,
            firstDepartment.getId(),
            createRequest("MATH", "Invalid", secondDegreeCycle, secondProgramPath)
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");

        ProgramFiliereResponse updated = programFiliereService.updateProgramFiliere(
            root,
            created.id(),
            new UpdateProgramFiliereRequest(
                "GL",
                "Software and Systems Engineering",
                firstDegreeCycle.getId(),
                firstProgramPath.getId()
            )
        );
        assertThat(updated.code()).isEqualTo("GL");

        assertThat(programFiliereService.deleteProgramFiliere(root, created.id()).success()).isTrue();
        assertThat(programFiliereRepository.findById(created.id())).isEmpty();
    }

    @Test
    void adminNeedsSpecificPermissionsAndCannotCrossEstablishments() {
        AuthenticatedUserPrincipal root = principal(
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null
        );
        ProgramFiliereResponse program = programFiliereService.createProgramFiliere(
            root,
            firstDepartment.getId(),
            createRequest("IL", "Software Engineering", firstDegreeCycle, firstProgramPath)
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
            .findByCodeIn(Set.of(PermissionCode.PROGRAM_FILIERE_UPDATE))
            .get(0);
        AdminPermissionGrant grant = new AdminPermissionGrant();
        grant.setAdmin(admin);
        grant.setPermission(updatePermission);
        adminPermissionGrantRepository.save(grant);

        AuthenticatedUserPrincipal adminPrincipal = principal(
            AccountRoleType.ADMIN,
            admin.getId(),
            firstEstablishment.getId()
        );

        assertThatThrownBy(() -> programFiliereService.getProgramFiliere(adminPrincipal, program.id()))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
        assertThat(programFiliereService.updateProgramFiliere(
            adminPrincipal,
            program.id(),
            new UpdateProgramFiliereRequest(
                "GL",
                "Updated Program",
                firstDegreeCycle.getId(),
                firstProgramPath.getId()
            )
        ).code()).isEqualTo("GL");
        assertThatThrownBy(() -> programFiliereService.deleteProgramFiliere(adminPrincipal, program.id()))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("403 FORBIDDEN");
        assertThatThrownBy(() -> programFiliereService.updateProgramFiliere(
            adminPrincipal,
            program.id(),
            new UpdateProgramFiliereRequest(
                "DENIED",
                "Denied",
                secondDegreeCycle.getId(),
                secondProgramPath.getId()
            )
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");
    }

    private CreateProgramFiliereRequest createRequest(
        String code,
        String name,
        DegreeCycle degreeCycle,
        ProgramPath programPath
    ) {
        return new CreateProgramFiliereRequest(code, name, degreeCycle.getId(), programPath.getId());
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

    private Department saveDepartment(Establishment establishment, String name) {
        Department department = new Department();
        department.setEstablishment(establishment);
        department.setName(name);
        return departmentRepository.save(department);
    }

    private DegreeCycle saveDegreeCycle(Establishment establishment, String name) {
        DegreeCycle degreeCycle = new DegreeCycle();
        degreeCycle.setEstablishment(establishment);
        degreeCycle.setName(name);
        return degreeCycleRepository.save(degreeCycle);
    }

    private ProgramPath saveProgramPath(Establishment establishment, String name) {
        ProgramPath programPath = new ProgramPath();
        programPath.setEstablishment(establishment);
        programPath.setName(name);
        return programPathRepository.save(programPath);
    }

    private void clearBusinessData() {
        adminPermissionGrantRepository.deleteAll();
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
}
