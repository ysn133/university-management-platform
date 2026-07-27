package com.platform.academicregistration.classassignment.application;

import com.platform.academicregistration.classassignment.domain.StudentClassAssignment;
import com.platform.academicregistration.classassignment.infrastructure.StudentClassAssignmentRepository;
import com.platform.academicregistration.classassignment.presentation.dto.AssignStudentClassRequest;
import com.platform.academicregistration.classassignment.presentation.dto.StudentClassAssignmentResponse;
import com.platform.academicregistration.registration.domain.AcademicRegistration;
import com.platform.academicregistration.registration.domain.AcademicRegistrationStatus;
import com.platform.academicregistration.registration.infrastructure.AcademicRegistrationRepository;
import com.platform.academicregistration.semesterregistration.domain.SemesterRegestration;
import com.platform.academicregistration.semesterregistration.infrastructer.SemesterRegestrationRepository;
import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.classgroup.domain.ClassGroup;
import com.platform.universitygovernance.classgroup.domain.ClassGroupStatus;
import com.platform.universitygovernance.classgroup.infrastructure.ClassGroupRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudentClassAssignmentService {

    private final AcademicRegistrationRepository academicRegistrationRepository;
    private final SemesterRegestrationRepository semesterRegistrationRepository;
    private final StudentClassAssignmentRepository classAssignmentRepository;
    private final ClassGroupRepository classGroupRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public StudentClassAssignmentService(
        AcademicRegistrationRepository academicRegistrationRepository,
        SemesterRegestrationRepository semesterRegistrationRepository,
        StudentClassAssignmentRepository classAssignmentRepository,
        ClassGroupRepository classGroupRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.academicRegistrationRepository = academicRegistrationRepository;
        this.semesterRegistrationRepository = semesterRegistrationRepository;
        this.classAssignmentRepository = classAssignmentRepository;
        this.classGroupRepository = classGroupRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public StudentClassAssignmentResponse assignStudentClass(
        AuthenticatedUserPrincipal principal,
        UUID academicRegistrationId,
        UUID semesterId,
        AssignStudentClassRequest request
    ) {
        AcademicRegistration registration = findAcademicRegistration(
            academicRegistrationId
        );
        requirePermission(
            principal,
            registration,
            PermissionCode.ACADEMIC_REGISTRATION_UPDATE
        );
        SemesterRegestration semesterRegistration = findSemesterRegistration(
            academicRegistrationId,
            semesterId
        );
        ClassGroup classGroup = findClassGroup(request.classGroupId());
        ensureAssignable(registration, semesterRegistration, classGroup);

        StudentClassAssignment assignment = classAssignmentRepository
            .findBySemesterRegistrationId(semesterRegistration.getId())
            .map(existing -> {
                if (!existing.getClassGroup().getId().equals(classGroup.getId())) {
                    throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "The semester registration already has a class group assignment"
                    );
                }
                return existing;
            })
            .orElseGet(StudentClassAssignment::new);
        assignment.setSemesterRegistration(semesterRegistration);
        assignment.setClassGroup(classGroup);
        return toResponse(classAssignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public StudentClassAssignmentResponse getStudentClassAssignment(
        AuthenticatedUserPrincipal principal,
        UUID academicRegistrationId,
        UUID semesterId
    ) {
        AcademicRegistration registration = findAcademicRegistration(
            academicRegistrationId
        );
        requirePermission(
            principal,
            registration,
            PermissionCode.ACADEMIC_REGISTRATION_VIEW
        );
        SemesterRegestration semesterRegistration = findSemesterRegistration(
            academicRegistrationId,
            semesterId
        );
        StudentClassAssignment assignment = classAssignmentRepository
            .findBySemesterRegistrationId(semesterRegistration.getId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Student class assignment not found"
            ));
        return toResponse(assignment);
    }

    private AcademicRegistration findAcademicRegistration(UUID registrationId) {
        return academicRegistrationRepository.findById(registrationId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic registration not found"
            ));
    }

    private SemesterRegestration findSemesterRegistration(
        UUID academicRegistrationId,
        UUID semesterId
    ) {
        return semesterRegistrationRepository
            .findByAcademicRegistrationIdAndSemesterId(
                academicRegistrationId,
                semesterId
            )
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Semester registration not found"
            ));
    }

    private ClassGroup findClassGroup(UUID classGroupId) {
        return classGroupRepository.findById(classGroupId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Class group not found"
            ));
    }

    private void ensureAssignable(
        AcademicRegistration registration,
        SemesterRegestration semesterRegistration,
        ClassGroup classGroup
    ) {
        boolean compatible = registration.getStatus() == AcademicRegistrationStatus.ACTIVE
            && classGroup.getStatus() == ClassGroupStatus.ACTIVE
            && registration.getAcademicYear().getId().equals(
                classGroup.getAcademicYear().getId()
            )
            && registration.getAcademicLevel().getId().equals(
                classGroup.getAcademicLevel().getId()
            )
            && semesterRegistration.getSemester().getAcademicLevel().getId().equals(
                classGroup.getAcademicLevel().getId()
            );

        if (!compatible) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Class group must match an active registration's academic year and level"
            );
        }
    }

    private void requirePermission(
        AuthenticatedUserPrincipal principal,
        AcademicRegistration registration,
        PermissionCode permissionCode
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            registration.getStudent().getEstablishment().getId(),
            permissionCode
        );
    }

    private StudentClassAssignmentResponse toResponse(
        StudentClassAssignment assignment
    ) {
        SemesterRegestration semesterRegistration = assignment
            .getSemesterRegistration();
        return new StudentClassAssignmentResponse(
            assignment.getId(),
            semesterRegistration.getAcademicRegistration().getId(),
            semesterRegistration.getId(),
            semesterRegistration.getSemester().getId(),
            assignment.getClassGroup().getId(),
            assignment.getCreatedAt(),
            assignment.getUpdatedAt()
        );
    }
}
