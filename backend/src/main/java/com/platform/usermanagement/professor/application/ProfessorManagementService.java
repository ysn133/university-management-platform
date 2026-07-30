package com.platform.usermanagement.professor.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.identityaccess.domain.AccountStatus;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.domain.Professor;
import com.platform.identityaccess.domain.UserAccount;
import com.platform.identityaccess.domain.UserProfile;
import com.platform.identityaccess.infrastructure.ProfessorRepository;
import com.platform.identityaccess.infrastructure.UserAccountRepository;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.usermanagement.professor.presentation.dto.CreateProfessorRequest;
import com.platform.usermanagement.professor.presentation.dto.CreateProfessorResponse;
import com.platform.usermanagement.professor.presentation.dto.ProfessorProfileResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfessorManagementService {

    private final EstablishmentRepository establishmentRepository;
    private final ProfessorRepository professorRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public ProfessorManagementService(
        EstablishmentRepository establishmentRepository,
        ProfessorRepository professorRepository,
        UserAccountRepository userAccountRepository,
        UserProfileRepository userProfileRepository,
        PasswordEncoder passwordEncoder,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.establishmentRepository = establishmentRepository;
        this.professorRepository = professorRepository;
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public CreateProfessorResponse createProfessor(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        CreateProfessorRequest request
    ) {
        requirePermission(principal, establishmentId, PermissionCode.PROFESSOR_CREATE);
        Establishment establishment = findEstablishment(establishmentId);

        if (request.hireDate() != null && request.hireDate().isBefore(request.birthDate())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Hire date cannot be before birth date"
            );
        }

        String universityEmail = request.universityEmail().trim().toLowerCase(Locale.ROOT);
        if (userAccountRepository.existsByUniversityEmail(universityEmail)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "University email already exists"
            );
        }

        String employeeNumber = request.employeeNumber().trim().toUpperCase(Locale.ROOT);
        if (professorRepository.existsByEmployeeNumberIgnoreCase(employeeNumber)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Employee number already exists"
            );
        }

        String cin = normalizeOptionalUppercase(request.cin());
        if (cin != null && userProfileRepository.existsByCinIgnoreCase(cin)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CIN already exists");
        }

        UserAccount account = new UserAccount();
        account.setUniversityEmail(universityEmail);
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setRole(AccountRoleType.PROFESSOR);
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

        Professor professor = new Professor();
        professor.setUserAccount(account);
        professor.setEstablishment(establishment);
        professor.setEmployeeNumber(employeeNumber);
        professor.setAcademicRank(normalizeOptional(request.academicRank()));
        professor.setHireDate(request.hireDate());
        professor.setMaximumWeeklyTeachingMinutes(request.maximumWeeklyTeachingMinutes());
        professor = professorRepository.save(professor);

        return new CreateProfessorResponse(
            professor.getId(),
            account.getId(),
            establishmentId,
            professor.getEmployeeNumber(),
            account.getRole()
        );
    }

    @Transactional(readOnly = true)
    public List<ProfessorProfileResponse> getProfessors(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        requirePermission(principal, establishmentId, PermissionCode.PROFESSOR_VIEW);
        findEstablishment(establishmentId);

        return professorRepository.findByEstablishmentIdOrderByCreatedAtAsc(establishmentId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ProfessorProfileResponse getProfessor(
        AuthenticatedUserPrincipal principal,
        UUID professorId
    ) {
        Professor professor = findProfessor(professorId);
        requirePermission(
            principal,
            professor.getEstablishment().getId(),
            PermissionCode.PROFESSOR_VIEW
        );
        return toResponse(professor);
    }

    private Establishment findEstablishment(UUID establishmentId) {
        return establishmentRepository.findById(establishmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Establishment not found"
            ));
    }

    private Professor findProfessor(UUID professorId) {
        return professorRepository.findById(professorId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Professor not found"
            ));
    }

    private UserProfile findProfile(Professor professor) {
        return userProfileRepository.findByUserAccountId(professor.getUserAccount().getId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Professor profile not found"
            ));
    }

    private void requirePermission(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        PermissionCode permissionCode
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            permissionCode
        );
    }

    private ProfessorProfileResponse toResponse(Professor professor) {
        UserAccount account = professor.getUserAccount();
        UserProfile profile = findProfile(professor);
        return new ProfessorProfileResponse(
            professor.getId(),
            account.getId(),
            professor.getEstablishment().getId(),
            professor.getEmployeeNumber(),
            professor.getAcademicRank(),
            professor.getHireDate(),
            professor.getMaximumWeeklyTeachingMinutes(),
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
}
