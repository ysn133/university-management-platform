package com.platform.academicregistration.classassignment.application;

import com.platform.academicregistration.classassignment.domain.StudentClassAssignment;
import com.platform.academicregistration.classassignment.infrastructure.StudentClassAssignmentRepository;
import com.platform.academicregistration.classassignment.presentation.dto.AssignStudentClassRequest;
import com.platform.academicregistration.classassignment.presentation.dto.BulkAssignStudentClassesRequest;
import com.platform.academicregistration.classassignment.presentation.dto.BulkClassAssignmentItemRequest;
import com.platform.academicregistration.classassignment.presentation.dto.BulkClassAssignmentResponse;
import com.platform.academicregistration.classassignment.presentation.dto.ClassGroupRosterGroupResponse;
import com.platform.academicregistration.classassignment.presentation.dto.ClassGroupRosterResponse;
import com.platform.academicregistration.classassignment.presentation.dto.StudentClassAssignmentResponse;
import com.platform.academicregistration.registration.domain.AcademicRegistration;
import com.platform.academicregistration.registration.domain.AcademicRegistrationStatus;
import com.platform.academicregistration.registration.infrastructure.AcademicRegistrationRepository;
import com.platform.academicregistration.semesterregistration.domain.SemesterRegistration;
import com.platform.academicregistration.semesterregistration.infrastructure.SemesterRegistrationRepository;
import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.classgroup.domain.ClassGroup;
import com.platform.universitygovernance.classgroup.domain.ClassGroupStatus;
import com.platform.universitygovernance.classgroup.infrastructure.ClassGroupRepository;
import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academiclevel.infrastructure.AcademicLevelRepository;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.semester.domain.Semester;
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudentClassAssignmentService {

    private final AcademicRegistrationRepository academicRegistrationRepository;
    private final SemesterRegistrationRepository semesterRegistrationRepository;
    private final StudentClassAssignmentRepository classAssignmentRepository;
    private final ClassGroupRepository classGroupRepository;
    private final AcademicLevelRepository academicLevelRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public StudentClassAssignmentService(
        AcademicRegistrationRepository academicRegistrationRepository,
        SemesterRegistrationRepository semesterRegistrationRepository,
        StudentClassAssignmentRepository classAssignmentRepository,
        ClassGroupRepository classGroupRepository,
        AcademicLevelRepository academicLevelRepository,
        AcademicYearRepository academicYearRepository,
        SemesterRepository semesterRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.academicRegistrationRepository = academicRegistrationRepository;
        this.semesterRegistrationRepository = semesterRegistrationRepository;
        this.classAssignmentRepository = classAssignmentRepository;
        this.classGroupRepository = classGroupRepository;
        this.academicLevelRepository = academicLevelRepository;
        this.academicYearRepository = academicYearRepository;
        this.semesterRepository = semesterRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional(readOnly = true)
    public ClassGroupRosterResponse getClassGroupRoster(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId,
        UUID academicYearId,
        UUID semesterId
    ) {
        AcademicLevel academicLevel = academicLevelRepository.findById(academicLevelId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic level not found"
            ));
        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic year not found"
            ));
        Semester semester = semesterRepository.findById(semesterId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Semester not found"
            ));
        UUID establishmentId = academicLevel.getProgramFiliere()
            .getDepartment()
            .getEstablishment()
            .getId();
        if (!establishmentId.equals(academicYear.getEstablishment().getId())
            || !semester.getAcademicLevel().getId().equals(academicLevelId)
            || !semester.getAcademicYear().getId().equals(academicYearId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Academic level, academic year, and semester must belong to the same context"
            );
        }
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.CLASS_GROUP_VIEW
        );

        List<AcademicRegistration> registrations = semesterRegistrationRepository
            .findBySemesterId(semesterId)
            .stream()
            .map(SemesterRegistration::getAcademicRegistration)
            .filter(registration ->
                registration.getStatus() == AcademicRegistrationStatus.ACTIVE
            )
            .distinct()
            .sorted(Comparator.comparing(registration ->
                registration.getStudent().getApogeeCode()
            ))
            .toList();
        Set<UUID> registrationIds = registrations.stream()
            .map(AcademicRegistration::getId)
            .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
        Map<UUID, UUID> groupIdByRegistrationId = new HashMap<>();
        classAssignmentRepository.findBySemesterRegistrationSemesterId(semesterId).stream()
            .filter(assignment -> registrationIds.contains(
                assignment.getSemesterRegistration().getAcademicRegistration().getId()
            ))
            .forEach(assignment -> groupIdByRegistrationId.put(
                assignment.getSemesterRegistration().getAcademicRegistration().getId(),
                assignment.getClassGroup().getId()
            ));

        List<ClassGroupRosterGroupResponse> groups = classGroupRepository
            .findByAcademicLevelIdAndAcademicYearIdOrderByNameAsc(academicLevelId, academicYearId)
            .stream()
            .filter(group -> group.getStatus() == ClassGroupStatus.ACTIVE)
            .map(group -> new ClassGroupRosterGroupResponse(
                group.getId(),
                group.getName(),
                registrations.stream()
                    .map(AcademicRegistration::getId)
                    .filter(registrationId -> group.getId().equals(
                        groupIdByRegistrationId.get(registrationId)
                    ))
                    .toList()
            ))
            .toList();
        List<UUID> unassignedRegistrationIds = registrations.stream()
            .map(AcademicRegistration::getId)
            .filter(registrationId -> !groupIdByRegistrationId.containsKey(registrationId))
            .toList();

        return new ClassGroupRosterResponse(
            academicLevelId,
            academicYearId,
            semesterId,
            registrations.size(),
            unassignedRegistrationIds,
            groups
        );
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
        SemesterRegistration semesterRegistration = findSemesterRegistration(
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
        SemesterRegistration semesterRegistration = findSemesterRegistration(
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

    @Transactional
    public BulkClassAssignmentResponse bulkAssignStudentClasses(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId,
        UUID academicYearId,
        BulkAssignStudentClassesRequest request
    ) {
        AcademicLevel academicLevel = academicLevelRepository.findByIdForUpdate(academicLevelId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic level not found"
            ));
        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic year not found"
            ));
        UUID establishmentId = academicLevel.getProgramFiliere()
            .getDepartment()
            .getEstablishment()
            .getId();
        if (!establishmentId.equals(academicYear.getEstablishment().getId())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Academic level and academic year must belong to the same establishment"
            );
        }
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.ACADEMIC_REGISTRATION_UPDATE
        );

        Map<UUID, UUID> groupIdByRegistrationId = new HashMap<>();
        for (BulkClassAssignmentItemRequest item : request.assignments()) {
            if (groupIdByRegistrationId.put(
                item.academicRegistrationId(),
                item.classGroupId()
            ) != null) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Each academic registration may appear only once"
                );
            }
        }

        Map<UUID, AcademicRegistration> registrations = loadRegistrations(
            groupIdByRegistrationId.keySet(),
            academicLevelId,
            academicYearId
        );
        Map<UUID, ClassGroup> groups = loadClassGroups(
            new HashSet<>(groupIdByRegistrationId.values()),
            academicLevelId,
            academicYearId
        );
        List<SemesterRegistration> semesterRegistrations = semesterRegistrationRepository
            .findByAcademicRegistrationIdIn(registrations.keySet())
            .stream()
            .filter(semesterRegistration ->
                semesterRegistration.getSemester().getAcademicLevel().getId()
                    .equals(academicLevelId)
                    && semesterRegistration.getSemester().getAcademicYear().getId()
                        .equals(academicYearId)
            )
            .toList();
        ensureEveryRegistrationHasSemesters(
            registrations.keySet(),
            semesterRegistrations
        );

        Map<UUID, StudentClassAssignment> existingBySemesterRegistrationId = new HashMap<>();
        classAssignmentRepository.findBySemesterRegistrationIdIn(
            semesterRegistrations.stream().map(SemesterRegistration::getId).toList()
        ).forEach(assignment -> existingBySemesterRegistrationId.put(
            assignment.getSemesterRegistration().getId(),
            assignment
        ));

        List<StudentClassAssignment> assignmentsToSave = new ArrayList<>();
        for (SemesterRegistration semesterRegistration : semesterRegistrations) {
            ClassGroup classGroup = groups.get(groupIdByRegistrationId.get(
                semesterRegistration.getAcademicRegistration().getId()
            ));
            StudentClassAssignment existing = existingBySemesterRegistrationId.get(
                semesterRegistration.getId()
            );
            if (existing != null) {
                if (!existing.getClassGroup().getId().equals(classGroup.getId())) {
                    existing.setClassGroup(classGroup);
                    assignmentsToSave.add(existing);
                }
                continue;
            }
            StudentClassAssignment assignment = new StudentClassAssignment();
            assignment.setSemesterRegistration(semesterRegistration);
            assignment.setClassGroup(classGroup);
            assignmentsToSave.add(assignment);
        }
        classAssignmentRepository.saveAll(assignmentsToSave);
        classAssignmentRepository.flush();

        return new BulkClassAssignmentResponse(
            academicLevelId,
            academicYearId,
            registrations.size(),
            assignmentsToSave.size()
        );
    }

    private Map<UUID, AcademicRegistration> loadRegistrations(
        Set<UUID> registrationIds,
        UUID academicLevelId,
        UUID academicYearId
    ) {
        Map<UUID, AcademicRegistration> registrations = new HashMap<>();
        academicRegistrationRepository.findAllById(registrationIds)
            .forEach(registration -> registrations.put(registration.getId(), registration));
        if (registrations.size() != registrationIds.size()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "One or more academic registrations were not found"
            );
        }
        boolean invalid = registrations.values().stream().anyMatch(registration ->
            registration.getStatus() != AcademicRegistrationStatus.ACTIVE
                || !registration.getAcademicLevel().getId().equals(academicLevelId)
                || !registration.getAcademicYear().getId().equals(academicYearId)
        );
        if (invalid) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Academic registrations must be active and match the selected level and year"
            );
        }
        return registrations;
    }

    private Map<UUID, ClassGroup> loadClassGroups(
        Set<UUID> classGroupIds,
        UUID academicLevelId,
        UUID academicYearId
    ) {
        Map<UUID, ClassGroup> groups = new HashMap<>();
        classGroupRepository.findAllById(classGroupIds)
            .forEach(group -> groups.put(group.getId(), group));
        if (groups.size() != classGroupIds.size()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "One or more class groups were not found"
            );
        }
        boolean invalid = groups.values().stream().anyMatch(group ->
            group.getStatus() != ClassGroupStatus.ACTIVE
                || !group.getAcademicLevel().getId().equals(academicLevelId)
                || !group.getAcademicYear().getId().equals(academicYearId)
        );
        if (invalid) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Class groups must be active and match the selected level and year"
            );
        }
        return groups;
    }

    private void ensureEveryRegistrationHasSemesters(
        Set<UUID> registrationIds,
        List<SemesterRegistration> semesterRegistrations
    ) {
        Set<UUID> registrationsWithSemesters = new HashSet<>();
        semesterRegistrations.forEach(semesterRegistration -> registrationsWithSemesters.add(
            semesterRegistration.getAcademicRegistration().getId()
        ));
        if (!registrationsWithSemesters.containsAll(registrationIds)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Every academic registration must have semester registrations before grouping"
            );
        }
    }

    private AcademicRegistration findAcademicRegistration(UUID registrationId) {
        return academicRegistrationRepository.findById(registrationId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic registration not found"
            ));
    }

    private SemesterRegistration findSemesterRegistration(
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
        SemesterRegistration semesterRegistration,
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
        SemesterRegistration semesterRegistration = assignment
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
