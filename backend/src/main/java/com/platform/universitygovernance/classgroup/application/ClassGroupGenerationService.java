package com.platform.universitygovernance.classgroup.application;

import com.platform.academicregistration.classassignment.domain.StudentClassAssignment;
import com.platform.academicregistration.classassignment.infrastructure.StudentClassAssignmentRepository;
import com.platform.academicregistration.registration.domain.AcademicRegistration;
import com.platform.academicregistration.registration.domain.AcademicRegistrationStatus;
import com.platform.academicregistration.registration.infrastructure.AcademicRegistrationRepository;
import com.platform.academicregistration.semesterregistration.domain.SemesterRegistration;
import com.platform.academicregistration.semesterregistration.infrastructure.SemesterRegistrationRepository;
import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academiclevel.infrastructure.AcademicLevelRepository;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.classgroup.domain.ClassGroup;
import com.platform.universitygovernance.classgroup.domain.ClassGroupStatus;
import com.platform.universitygovernance.classgroup.infrastructure.ClassGroupRepository;
import com.platform.universitygovernance.classgroup.presentation.dto.ClassGroupGenerationResponse;
import com.platform.universitygovernance.classgroup.presentation.dto.ClassGroupRebalanceResponse;
import com.platform.universitygovernance.classgroup.presentation.dto.GenerateClassGroupsRequest;
import com.platform.universitygovernance.classgroup.presentation.dto.GeneratedClassGroupResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClassGroupGenerationService {

    private final AcademicLevelRepository academicLevelRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AcademicRegistrationRepository academicRegistrationRepository;
    private final SemesterRegistrationRepository semesterRegistrationRepository;
    private final ClassGroupRepository classGroupRepository;
    private final StudentClassAssignmentRepository classAssignmentRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public ClassGroupGenerationService(
        AcademicLevelRepository academicLevelRepository,
        AcademicYearRepository academicYearRepository,
        AcademicRegistrationRepository academicRegistrationRepository,
        SemesterRegistrationRepository semesterRegistrationRepository,
        ClassGroupRepository classGroupRepository,
        StudentClassAssignmentRepository classAssignmentRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.academicLevelRepository = academicLevelRepository;
        this.academicYearRepository = academicYearRepository;
        this.academicRegistrationRepository = academicRegistrationRepository;
        this.semesterRegistrationRepository = semesterRegistrationRepository;
        this.classGroupRepository = classGroupRepository;
        this.classAssignmentRepository = classAssignmentRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public ClassGroupGenerationResponse generateClassGroups(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId,
        UUID academicYearId,
        GenerateClassGroupsRequest request
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
            PermissionCode.CLASS_GROUP_CREATE
        );
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.ACADEMIC_REGISTRATION_UPDATE
        );
        validateConfiguration(request);

        if (!classGroupRepository.findByAcademicLevelIdAndAcademicYearIdOrderByNameAsc(
            academicLevelId,
            academicYearId
        ).isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Automatic generation requires a level and year with no existing class groups"
            );
        }

        List<AcademicRegistration> registrations = academicRegistrationRepository
            .findByAcademicLevelIdAndAcademicYearIdAndStatusOrderByStudentApogeeCodeAsc(
                academicLevelId,
                academicYearId,
                AcademicRegistrationStatus.ACTIVE
            );
        if (registrations.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No active academic registrations are available for group generation"
            );
        }

        int groupCount = divideRoundingUp(registrations.size(), request.maximumGroupSize());
        int smallestGroupSize = registrations.size() / groupCount;
        if (groupCount > 1 && smallestGroupSize < request.minimumGroupSize()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The cohort cannot satisfy both the minimum and maximum group sizes"
            );
        }

        List<ClassGroup> groups = createGroups(academicLevel, academicYear, groupCount);
        Map<UUID, ClassGroup> groupByRegistrationId = new HashMap<>();
        int[] studentCounts = new int[groupCount];
        for (int index = 0; index < registrations.size(); index++) {
            int groupIndex = index % groupCount;
            groupByRegistrationId.put(registrations.get(index).getId(), groups.get(groupIndex));
            studentCounts[groupIndex]++;
        }

        List<UUID> registrationIds = registrations.stream()
            .map(AcademicRegistration::getId)
            .toList();
        List<SemesterRegistration> semesterRegistrations = semesterRegistrationRepository
            .findByAcademicRegistrationIdIn(registrationIds)
            .stream()
            .filter(semesterRegistration ->
                semesterRegistration.getSemester().getAcademicLevel().getId()
                    .equals(academicLevelId)
                    && semesterRegistration.getSemester().getAcademicYear().getId()
                        .equals(academicYearId)
            )
            .toList();
        ensureEveryRegistrationHasSemesters(
            registrationIds,
            semesterRegistrations
        );
        ensureNoExistingAssignments(semesterRegistrations);

        List<StudentClassAssignment> assignments = new ArrayList<>();
        for (SemesterRegistration semesterRegistration : semesterRegistrations) {
            StudentClassAssignment assignment = new StudentClassAssignment();
            assignment.setSemesterRegistration(semesterRegistration);
            assignment.setClassGroup(groupByRegistrationId.get(
                semesterRegistration.getAcademicRegistration().getId()
            ));
            assignments.add(assignment);
        }
        classAssignmentRepository.saveAll(assignments);
        classAssignmentRepository.flush();

        List<GeneratedClassGroupResponse> generatedGroups = new ArrayList<>();
        for (int index = 0; index < groups.size(); index++) {
            generatedGroups.add(new GeneratedClassGroupResponse(
                groups.get(index).getId(),
                groups.get(index).getName(),
                studentCounts[index]
            ));
        }
        return new ClassGroupGenerationResponse(
            academicLevelId,
            academicYearId,
            registrations.size(),
            assignments.size(),
            generatedGroups
        );
    }

    @Transactional
    public ClassGroupRebalanceResponse rebalanceClassGroups(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId,
        UUID academicYearId,
        GenerateClassGroupsRequest request
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
            PermissionCode.CLASS_GROUP_UPDATE
        );
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.CLASS_GROUP_CREATE
        );
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.ACADEMIC_REGISTRATION_UPDATE
        );
        validateConfiguration(request);

        List<AcademicRegistration> registrations = academicRegistrationRepository
            .findByAcademicLevelIdAndAcademicYearIdAndStatusOrderByStudentApogeeCodeAsc(
                academicLevelId,
                academicYearId,
                AcademicRegistrationStatus.ACTIVE
            );
        if (registrations.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No active academic registrations are available for group rebalancing"
            );
        }

        int groupCount = divideRoundingUp(registrations.size(), request.maximumGroupSize());
        int smallestGroupSize = registrations.size() / groupCount;
        if (groupCount > 1 && smallestGroupSize < request.minimumGroupSize()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The cohort cannot satisfy both the minimum and maximum group sizes"
            );
        }

        List<ClassGroup> allGroups = new ArrayList<>(classGroupRepository
            .findByAcademicLevelIdAndAcademicYearIdOrderByNameAsc(academicLevelId, academicYearId));
        List<ClassGroup> activeGroups = reconcileGroups(
            academicLevel,
            academicYear,
            allGroups,
            groupCount
        );

        Map<UUID, ClassGroup> groupByRegistrationId = new HashMap<>();
        int[] studentCounts = new int[groupCount];
        for (int index = 0; index < registrations.size(); index++) {
            int groupIndex = index % groupCount;
            groupByRegistrationId.put(registrations.get(index).getId(), activeGroups.get(groupIndex));
            studentCounts[groupIndex]++;
        }

        List<UUID> registrationIds = registrations.stream()
            .map(AcademicRegistration::getId)
            .toList();
        List<SemesterRegistration> semesterRegistrations = semesterRegistrationRepository
            .findByAcademicRegistrationIdIn(registrationIds)
            .stream()
            .filter(semesterRegistration ->
                semesterRegistration.getSemester().getAcademicLevel().getId().equals(academicLevelId)
                    && semesterRegistration.getSemester().getAcademicYear().getId().equals(academicYearId)
            )
            .toList();
        ensureEveryRegistrationHasSemesters(registrationIds, semesterRegistrations);

        Map<UUID, StudentClassAssignment> existingBySemesterRegistrationId = new HashMap<>();
        classAssignmentRepository.findBySemesterRegistrationIdIn(
            semesterRegistrations.stream().map(SemesterRegistration::getId).toList()
        ).forEach(assignment -> existingBySemesterRegistrationId.put(
            assignment.getSemesterRegistration().getId(),
            assignment
        ));
        List<StudentClassAssignment> assignmentsToSave = new ArrayList<>();
        for (SemesterRegistration semesterRegistration : semesterRegistrations) {
            ClassGroup targetGroup = groupByRegistrationId.get(
                semesterRegistration.getAcademicRegistration().getId()
            );
            StudentClassAssignment assignment = existingBySemesterRegistrationId.get(
                semesterRegistration.getId()
            );
            if (assignment == null) {
                assignment = new StudentClassAssignment();
                assignment.setSemesterRegistration(semesterRegistration);
            } else if (assignment.getClassGroup().getId().equals(targetGroup.getId())) {
                continue;
            }
            assignment.setClassGroup(targetGroup);
            assignmentsToSave.add(assignment);
        }
        classAssignmentRepository.saveAll(assignmentsToSave);
        classAssignmentRepository.flush();

        List<GeneratedClassGroupResponse> groups = new ArrayList<>();
        for (int index = 0; index < activeGroups.size(); index++) {
            groups.add(new GeneratedClassGroupResponse(
                activeGroups.get(index).getId(),
                activeGroups.get(index).getName(),
                studentCounts[index]
            ));
        }
        return new ClassGroupRebalanceResponse(
            academicLevelId,
            academicYearId,
            registrations.size(),
            assignmentsToSave.size(),
            groups
        );
    }

    private void validateConfiguration(GenerateClassGroupsRequest request) {
        if (request.minimumGroupSize() < 1 || request.maximumGroupSize() < 1) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Minimum and maximum group sizes must be greater than zero"
            );
        }
        if (request.minimumGroupSize() > request.maximumGroupSize()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Minimum group size cannot exceed maximum group size"
            );
        }
    }

    private int divideRoundingUp(int value, int divisor) {
        return (value + divisor - 1) / divisor;
    }

    private List<ClassGroup> createGroups(
        AcademicLevel academicLevel,
        AcademicYear academicYear,
        int groupCount
    ) {
        List<ClassGroup> groups = new ArrayList<>();
        for (int index = 0; index < groupCount; index++) {
            ClassGroup group = new ClassGroup();
            group.setAcademicLevel(academicLevel);
            group.setAcademicYear(academicYear);
            group.setName("Group " + groupSuffix(index));
            group.setStatus(ClassGroupStatus.ACTIVE);
            groups.add(group);
        }
        groups = classGroupRepository.saveAll(groups);
        classGroupRepository.flush();
        return groups;
    }

    private String groupSuffix(int zeroBasedIndex) {
        StringBuilder suffix = new StringBuilder();
        int value = zeroBasedIndex + 1;
        while (value > 0) {
            value--;
            suffix.append((char) ('A' + value % 26));
            value /= 26;
        }
        return suffix.reverse().toString();
    }

    private List<ClassGroup> reconcileGroups(
        AcademicLevel academicLevel,
        AcademicYear academicYear,
        List<ClassGroup> allGroups,
        int groupCount
    ) {
        Map<String, ClassGroup> groupByName = new HashMap<>();
        allGroups.forEach(group -> groupByName.put(
            group.getName().toLowerCase(Locale.ROOT),
            group
        ));

        List<ClassGroup> selectedGroups = new ArrayList<>();
        for (int index = 0; index < groupCount; index++) {
            String name = "Group " + groupSuffix(index);
            ClassGroup group = groupByName.get(name.toLowerCase(Locale.ROOT));
            if (group == null) {
                group = new ClassGroup();
                group.setAcademicLevel(academicLevel);
                group.setAcademicYear(academicYear);
                group.setName(name);
                allGroups.add(group);
            }
            group.setStatus(ClassGroupStatus.ACTIVE);
            selectedGroups.add(group);
        }

        Set<ClassGroup> selectedGroupSet = new HashSet<>(selectedGroups);
        allGroups.stream()
            .filter(group -> !selectedGroupSet.contains(group))
            .forEach(group -> group.setStatus(ClassGroupStatus.INACTIVE));
        classGroupRepository.saveAll(allGroups);
        classGroupRepository.flush();
        return selectedGroups;
    }

    private void ensureEveryRegistrationHasSemesters(
        List<UUID> registrationIds,
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

    private void ensureNoExistingAssignments(
        List<SemesterRegistration> semesterRegistrations
    ) {
        List<UUID> semesterRegistrationIds = semesterRegistrations.stream()
            .map(SemesterRegistration::getId)
            .toList();
        if (!classAssignmentRepository.findBySemesterRegistrationIdIn(
            semesterRegistrationIds
        ).isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Class assignments already exist for this level and year"
            );
        }
    }
}
