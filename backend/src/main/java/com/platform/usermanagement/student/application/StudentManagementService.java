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
import com.platform.usermanagement.student.presentation.dto.UpdateStudentRequest;
import com.platform.usermanagement.shared.presentation.dto.ResetManagedPasswordRequest;
import com.platform.shared.presentation.ActionResponse;
import java.time.LocalDate;
import java.util.Comparator;
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

        if (request.initialEnrollmentDate().isBefore(request.birthDate())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Initial enrollment date cannot be before birth date"
            );
        }

        String universityEmail = request.universityEmail().trim().toLowerCase(Locale.ROOT);
        if (userAccountRepository.existsByUniversityEmail(universityEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "University email already exists");
        }

        String apogeeCode = request.apogeeCode().trim().toUpperCase(Locale.ROOT);
        if (studentRepository.existsByApogeeCodeIgnoreCase(apogeeCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Apogee code already exists");
        }

        String nationalStudentCode = normalizeOptionalUppercase(request.nationalStudentCode());
        if (nationalStudentCode != null
            && studentRepository.existsByNationalStudentCodeIgnoreCase(nationalStudentCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Massar/CNE code already exists");
        }

        String cin = normalizeOptionalUppercase(request.cin());
        if (cin != null && userProfileRepository.existsByCinIgnoreCase(cin)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CIN already exists");
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
        profile.setPlaceOfBirth(request.placeOfBirth().trim());
        profile.setNationality(request.nationality().trim());
        profile.setCin(cin);
        profile.setSex(request.sex());
        profile.setPhoneNumber(normalizeOptional(request.phoneNumber()));
        userProfileRepository.save(profile);

        Student student = new Student();
        student.setUserAccount(account);
        student.setEstablishment(establishment);
        student.setApogeeCode(apogeeCode);
        student.setNationalStudentCode(nationalStudentCode);
        student.setInitialEnrollmentDate(request.initialEnrollmentDate());
        student = studentRepository.save(student);

        return new CreateStudentResponse(
            student.getId(),
            account.getId(),
            establishmentId,
            student.getApogeeCode(),
            account.getRole()
        );
    }

    @Transactional(readOnly = true)
    public List<StudentProfileResponse> getStudents(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        return getStudents(principal, establishmentId, null, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<StudentProfileResponse> getStudents(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        String query,
        AccountStatus status,
        LocalDate enrolledFrom,
        LocalDate enrolledTo
    ) {
        requirePermission(principal, establishmentId, PermissionCode.STUDENT_VIEW);
        findEstablishment(establishmentId);

        String normalizedQuery = normalizeQuery(query);
        return studentRepository.findByEstablishmentIdOrderByCreatedAtAsc(establishmentId).stream()
            .filter(student -> status == null || student.getUserAccount().getAccountStatus() == status)
            .filter(student -> enrolledFrom == null
                || student.getInitialEnrollmentDate() != null
                    && !student.getInitialEnrollmentDate().isBefore(enrolledFrom))
            .filter(student -> enrolledTo == null
                || student.getInitialEnrollmentDate() != null
                    && !student.getInitialEnrollmentDate().isAfter(enrolledTo))
            .filter(student -> normalizedQuery == null
                || matchesQuery(student, findProfile(student), normalizedQuery))
            .map(this::toResponse)
            .sorted(Comparator.comparing(StudentProfileResponse::lastName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(StudentProfileResponse::firstName, String.CASE_INSENSITIVE_ORDER))
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

    @Transactional
    public StudentProfileResponse updateStudent(
        AuthenticatedUserPrincipal principal,
        UUID studentId,
        UpdateStudentRequest request
    ) {
        Student student = findStudent(studentId);
        requirePermission(principal, student.getEstablishment().getId(), PermissionCode.STUDENT_UPDATE);
        if (request.initialEnrollmentDate().isBefore(request.birthDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Initial enrollment date cannot be before birth date");
        }

        UserAccount account = student.getUserAccount();
        UserProfile profile = findProfile(student);
        String email = request.universityEmail().trim().toLowerCase(Locale.ROOT);
        String apogee = request.apogeeCode().trim().toUpperCase(Locale.ROOT);
        String nationalCode = normalizeOptionalUppercase(request.nationalStudentCode());
        String cin = normalizeOptionalUppercase(request.cin());

        if (userAccountRepository.existsByUniversityEmailAndIdNot(email, account.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "University email already exists");
        }
        if (studentRepository.existsByApogeeCodeIgnoreCaseAndIdNot(apogee, student.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Apogee code already exists");
        }
        if (nationalCode != null
            && studentRepository.existsByNationalStudentCodeIgnoreCaseAndIdNot(nationalCode, student.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Massar/CNE code already exists");
        }
        if (cin != null && userProfileRepository.existsByCinIgnoreCaseAndIdNot(cin, profile.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CIN already exists");
        }

        account.setUniversityEmail(email);
        student.setApogeeCode(apogee);
        student.setNationalStudentCode(nationalCode);
        student.setInitialEnrollmentDate(request.initialEnrollmentDate());
        profile.setFirstName(request.firstName().trim());
        profile.setLastName(request.lastName().trim());
        profile.setBirthDate(request.birthDate());
        profile.setPlaceOfBirth(request.placeOfBirth().trim());
        profile.setNationality(request.nationality().trim());
        profile.setCin(cin);
        profile.setSex(request.sex());
        profile.setPhoneNumber(normalizeOptional(request.phoneNumber()));
        return toResponse(student);
    }

    @Transactional
    public ActionResponse resetPassword(AuthenticatedUserPrincipal principal, UUID studentId, ResetManagedPasswordRequest request) {
        UserAccount account = findManagedStudent(principal, studentId).getUserAccount();
        if (passwordEncoder.matches(request.newPassword(), account.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from the current password");
        }
        account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        return new ActionResponse(true, "Password has been reset");
    }

    @Transactional
    public ActionResponse lockAccount(AuthenticatedUserPrincipal principal, UUID studentId) {
        UserAccount account = findManagedStudent(principal, studentId).getUserAccount();
        ensureCanLock(account);
        account.setAccountStatus(AccountStatus.LOCKED);
        return new ActionResponse(true, "Account locked");
    }

    @Transactional
    public ActionResponse unlockAccount(AuthenticatedUserPrincipal principal, UUID studentId) {
        UserAccount account = findManagedStudent(principal, studentId).getUserAccount();
        if (account.getAccountStatus() != AccountStatus.LOCKED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only a locked account can be unlocked");
        }
        account.setAccountStatus(AccountStatus.ACTIVE);
        return new ActionResponse(true, "Account unlocked");
    }

    @Transactional
    public ActionResponse deactivateAccount(AuthenticatedUserPrincipal principal, UUID studentId) {
        UserAccount account = findManagedStudent(principal, studentId).getUserAccount();
        if (account.getAccountStatus() == AccountStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Archived account cannot be deactivated");
        }
        account.setAccountStatus(AccountStatus.DEACTIVATED);
        return new ActionResponse(true, "Account deactivated");
    }

    @Transactional
    public ActionResponse archiveAccount(AuthenticatedUserPrincipal principal, UUID studentId) {
        findManagedStudent(principal, studentId).getUserAccount().setAccountStatus(AccountStatus.ARCHIVED);
        return new ActionResponse(true, "Account archived");
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

    private Student findManagedStudent(AuthenticatedUserPrincipal principal, UUID studentId) {
        Student student = findStudent(studentId);
        requirePermission(principal, student.getEstablishment().getId(), PermissionCode.STUDENT_ACCOUNT_MANAGE);
        return student;
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
            student.getApogeeCode(),
            student.getNationalStudentCode(),
            student.getInitialEnrollmentDate(),
            account.getUniversityEmail(),
            account.getRole(),
            account.getAccountStatus(),
            profile.getFirstName(),
            profile.getLastName(),
            profile.getBirthDate(),
            profile.getPlaceOfBirth(),
            profile.getNationality(),
            profile.getCin(),
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

    private String normalizeOptionalUppercase(String value) {
        String normalized = normalizeOptional(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeQuery(String value) {
        String normalized = normalizeOptional(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private boolean matchesQuery(Student student, UserProfile profile, String query) {
        String fullName = (profile.getFirstName() + " " + profile.getLastName()).toLowerCase(Locale.ROOT);
        return fullName.contains(query)
            || student.getUserAccount().getUniversityEmail().toLowerCase(Locale.ROOT).contains(query)
            || student.getApogeeCode().toLowerCase(Locale.ROOT).contains(query)
            || student.getNationalStudentCode() != null && student.getNationalStudentCode().toLowerCase(Locale.ROOT).contains(query)
            || profile.getCin() != null && profile.getCin().toLowerCase(Locale.ROOT).contains(query);
    }

    private void ensureCanLock(UserAccount account) {
        if (account.getAccountStatus() == AccountStatus.DEACTIVATED
            || account.getAccountStatus() == AccountStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inactive account cannot be locked");
        }
    }
}
