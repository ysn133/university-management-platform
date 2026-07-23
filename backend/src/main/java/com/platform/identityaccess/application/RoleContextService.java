package com.platform.identityaccess.application;

import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.Admin;
import com.platform.identityaccess.domain.Professor;
import com.platform.identityaccess.domain.RootSuperAdmin;
import com.platform.identityaccess.domain.SuperAdmin;
import com.platform.identityaccess.domain.Student;
import com.platform.identityaccess.domain.UserAccount;
import com.platform.identityaccess.infrastructure.AdminRepository;
import com.platform.identityaccess.infrastructure.ProfessorRepository;
import com.platform.identityaccess.infrastructure.RootSuperAdminRepository;
import com.platform.identityaccess.infrastructure.StudentRepository;
import com.platform.identityaccess.infrastructure.SuperAdminRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RoleContextService {

    private final RootSuperAdminRepository rootSuperAdminRepository;
    private final SuperAdminRepository superAdminRepository;
    private final AdminRepository adminRepository;
    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;

    public RoleContextService(
        RootSuperAdminRepository rootSuperAdminRepository,
        SuperAdminRepository superAdminRepository,
        AdminRepository adminRepository,
        ProfessorRepository professorRepository,
        StudentRepository studentRepository
    ) {
        this.rootSuperAdminRepository = rootSuperAdminRepository;
        this.superAdminRepository = superAdminRepository;
        this.adminRepository = adminRepository;
        this.professorRepository = professorRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public RoleContext loadRoleContext(UserAccount account, AccountRoleType requestedRole) {
        if (account.getRole() != requestedRole) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role does not match the account");
        }

        return switch (requestedRole) {
            case ROOT_SUPER_ADMIN -> buildRootSuperAdminContext(account);
            case SUPER_ADMIN -> buildSuperAdminContext(account);
            case ADMIN -> buildAdminContext(account);
            case STUDENT -> buildStudentContext(account);
            case PROFESSOR -> buildProfessorContext(account);
        };
    }

    private RoleContext buildRootSuperAdminContext(UserAccount account) {
        RootSuperAdmin rootSuperAdmin = rootSuperAdminRepository.findByUserAccountId(account.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Root super admin profile not found"));

        return new RoleContext(rootSuperAdmin.getId(), null);
    }

    private RoleContext buildSuperAdminContext(UserAccount account) {
        SuperAdmin superAdmin = superAdminRepository.findByUserAccountId(account.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Super admin profile not found"));

        return new RoleContext(superAdmin.getId(), superAdmin.getEstablishment().getId());
    }

    private RoleContext buildAdminContext(UserAccount account) {
        Admin admin = adminRepository.findByUserAccountId(account.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin profile not found"));

        return new RoleContext(admin.getId(), admin.getEstablishment().getId());
    }

    private RoleContext buildStudentContext(UserAccount account) {
        Student student = studentRepository.findByUserAccountId(account.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Student profile not found"));

        return new RoleContext(student.getId(), student.getEstablishment().getId());
    }

    private RoleContext buildProfessorContext(UserAccount account) {
        Professor professor = professorRepository.findByUserAccountId(account.getId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Professor profile not found"
            ));

        return new RoleContext(professor.getId(), professor.getEstablishment().getId());
    }
}
