package com.platform.usermanagement.student.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.Student;
import com.platform.identityaccess.domain.UserAccount;
import com.platform.identityaccess.domain.UserProfile;
import com.platform.identityaccess.infrastructure.StudentRepository;
import com.platform.identityaccess.infrastructure.UserAccountRepository;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.usermanagement.student.presentation.dto.CreateStudentRequest;
import com.platform.usermanagement.student.presentation.dto.CreateStudentResponse;
import com.platform.usermanagement.student.presentation.dto.StudentProfileResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudentManagementService {

    private final EstablishmentRepository establishmentRepository;
    private final StudentRepository studentRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public StudentManagementService(
        EstablishmentRepository establishmentRepository,
        StudentRepository studentRepository,
        UserAccountRepository userAccountRepository,
        UserProfileRepository userProfileRepository,
        PasswordEncoder passwordEncoder,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.establishmentRepository = establishmentRepository;
        this.studentRepository = studentRepository;
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public CreateStudentResponse createStudent(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        CreateStudentRequest request
    ) {
        requirePermission(principal, establishmentId, PermissionCode.STUDENT_CREATE);
        Establishment establishment = findEstablishment(establishmentId);

        String universityEmail = request.universityEmail().trim().toLowerCase(Locale.ROOT);
        if (userAccountRepository.existsByUniversityEmail(universityEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "University email already exists");
        }

        UserAccount account = new UserAccount();
        account.setUniversityEmail(universityEmail);
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setRole(AccountRoleType.STUDENT);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account = userAccountRepository.save(account);

        UserProfile profile = new UserProfile();
        profile.setUserAccount(account);
        profile.setFirstName(request.firstName().trim());
        profile.setLastName(request.lastName().trim());
        profile.setBirthDate(request.birthDate());
        profile.setSex(request.sex());
        profile.setPhoneNumber(normalizeOptional(request.phoneNumber()));
        userProfileRepository.save(profile);

        Student student = new Student();
        student.setUserAccount(account);
        student.setEstablishment(establishment);
        student = studentRepository.save(student);

        return new CreateStudentResponse(
            student.getId(),
            account.getId(),
            establishmentId,
            account.getRole()
        );
    }

    @Transactional(readOnly = true)
    public List<StudentProfileResponse> getStudents(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        requirePermission(principal, establishmentId, PermissionCode.STUDENT_VIEW);
        findEstablishment(establishmentId);

        return studentRepository.findByEstablishmentIdOrderByCreatedAtAsc(establishmentId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public StudentProfileResponse getStudent(
        AuthenticatedUserPrincipal principal,
        UUID studentId
    ) {
        Student student = findStudent(studentId);
        requirePermission(
            principal,
            student.getEstablishment().getId(),
            PermissionCode.STUDENT_VIEW
        );
        return toResponse(student);
    }

    private Establishment findEstablishment(UUID establishmentId) {
        return establishmentRepository.findById(establishmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Establishment not found"
            ));
    }

    private Student findStudent(UUID studentId) {
        return studentRepository.findById(studentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Student not found"
            ));
    }

    private UserProfile findProfile(Student student) {
        return userProfileRepository.findByUserAccountId(student.getUserAccount().getId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Student profile not found"
            ));
    }

    private void requirePermission(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        PermissionCode permissionCode
    ) {
        permissionAuthorizationService.requirePermission(principal, establishmentId, permissionCode);
    }

    private StudentProfileResponse toResponse(Student student) {
        UserAccount account = student.getUserAccount();
        UserProfile profile = findProfile(student);
        return new StudentProfileResponse(
            student.getId(),
            account.getId(),
            student.getEstablishment().getId(),
            account.getUniversityEmail(),
            account.getRole(),
            account.getAccountStatus(),
            profile.getFirstName(),
            profile.getLastName(),
            profile.getBirthDate(),
            profile.getSex(),
            profile.getPhoneNumber(),
            profile.getProfilePicturePath()
        );
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
