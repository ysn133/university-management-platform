package com.platform.scheduling.teachinggroup.application;

import com.platform.academicregistration.classassignment.domain.StudentClassAssignment;
import com.platform.academicregistration.classassignment.infrastructure.StudentClassAssignmentRepository;
import com.platform.academicregistration.registration.domain.AcademicRegistrationStatus;
import com.platform.academicregistration.semesterregistration.domain.SemesterRegestration;
import com.platform.academicregistration.semesterregistration.infrastructer.SemesterRegestrationRepository;
import com.platform.scheduling.teachinggroup.domain.TeachingGroup;
import com.platform.scheduling.teachinggroup.domain.TeachingGroupMembership;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupMembershipRepository;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupRepository;
import com.platform.teachingrequirement.infrastructure.TeachingRequirementRepository;
import com.platform.universitygovernance.classgroup.domain.ClassGroup;
import com.platform.universitygovernance.moduleteachingcomponent.domain.ModuleTeachingComponent;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode;
import com.platform.universitygovernance.moduleteachingcomponent.infrastructure.ModuleTeachingComponentRepository;
import com.platform.universitygovernance.semester.domain.Semester;
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TeachingGroupGenerationService {

    private final SemesterRepository semesterRepository;
    private final SemesterRegestrationRepository semesterRegistrationRepository;
    private final StudentClassAssignmentRepository classAssignmentRepository;
    private final ModuleTeachingComponentRepository componentRepository;
    private final TeachingGroupRepository teachingGroupRepository;
    private final TeachingGroupMembershipRepository membershipRepository;
    private final TeachingRequirementRepository requirementRepository;

    public TeachingGroupGenerationService(
        SemesterRepository semesterRepository,
        SemesterRegestrationRepository semesterRegistrationRepository,
        StudentClassAssignmentRepository classAssignmentRepository,
        ModuleTeachingComponentRepository componentRepository,
        TeachingGroupRepository teachingGroupRepository,
        TeachingGroupMembershipRepository membershipRepository,
        TeachingRequirementRepository requirementRepository
    ) {
        this.semesterRepository = semesterRepository;
        this.semesterRegistrationRepository = semesterRegistrationRepository;
        this.classAssignmentRepository = classAssignmentRepository;
        this.componentRepository = componentRepository;
        this.teachingGroupRepository = teachingGroupRepository;
        this.membershipRepository = membershipRepository;
        this.requirementRepository = requirementRepository;
    }

    @Transactional
    public List<TeachingGroup> generateForSemester(UUID semesterId) {
        if (requirementRepository.existsByTeachingGroupSemesterId(semesterId)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Teaching groups cannot be regenerated after requirements exist"
            );
        }
        Semester semester = semesterRepository.findById(semesterId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Semester not found"
            ));

        List<SemesterRegestration> activeRegistrations = semesterRegistrationRepository
            .findBySemesterId(semesterId)
            .stream()
            .filter(registration -> registration.getAcademicRegistration().getStatus()
                == AcademicRegistrationStatus.ACTIVE)
            .sorted(Comparator.comparing(registration ->
                registration.getAcademicRegistration().getStudent().getApogeeCode()))
            .toList();

        Map<UUID, StudentClassAssignment> assignmentsByRegistration = new HashMap<>();
        for (StudentClassAssignment assignment :
            classAssignmentRepository.findBySemesterRegistrationSemesterId(semesterId)) {
            assignmentsByRegistration.put(
                assignment.getSemesterRegistration().getId(),
                assignment
            );
        }
        for (SemesterRegestration registration : activeRegistrations) {
            if (!assignmentsByRegistration.containsKey(registration.getId())) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Every active semester registration must have a class assignment"
                );
            }
        }

        clearExistingGroups(semesterId);
        if (activeRegistrations.isEmpty()) {
            return List.of();
        }

        Map<ClassGroup, List<SemesterRegestration>> registrationsByClass = new LinkedHashMap<>();
        activeRegistrations.stream()
            .map(registration -> assignmentsByRegistration.get(registration.getId()).getClassGroup())
            .distinct()
            .sorted(Comparator.comparing(ClassGroup::getName).thenComparing(ClassGroup::getId))
            .forEach(classGroup -> registrationsByClass.put(classGroup, new ArrayList<>()));
        for (SemesterRegestration registration : activeRegistrations) {
            ClassGroup classGroup = assignmentsByRegistration
                .get(registration.getId())
                .getClassGroup();
            registrationsByClass.get(classGroup).add(registration);
        }

        Integer subgroupCapacity = findSubgroupCapacity(semesterId);
        List<TeachingGroup> generatedGroups = new ArrayList<>();
        List<TeachingGroupMembership> generatedMemberships = new ArrayList<>();

        TeachingGroup cohort = createGroup(
            semester,
            null,
            "Whole Cohort",
            TeachingAudienceMode.WHOLE_COHORT
        );
        generatedGroups.add(cohort);
        addMemberships(cohort, activeRegistrations, generatedMemberships);

        for (Map.Entry<ClassGroup, List<SemesterRegestration>> entry :
            registrationsByClass.entrySet()) {
            ClassGroup classGroup = entry.getKey();
            List<SemesterRegestration> classRegistrations = entry.getValue();

            TeachingGroup classAudience = createGroup(
                semester,
                classGroup,
                classGroup.getName(),
                TeachingAudienceMode.CLASS_GROUP
            );
            generatedGroups.add(classAudience);
            addMemberships(classAudience, classRegistrations, generatedMemberships);

            if (subgroupCapacity != null) {
                List<List<SemesterRegestration>> subgroups = splitBalanced(
                    classRegistrations,
                    subgroupCapacity
                );
                for (int index = 0; index < subgroups.size(); index++) {
                    TeachingGroup subgroup = createGroup(
                        semester,
                        classGroup,
                        classGroup.getName() + (index + 1),
                        TeachingAudienceMode.SUBGROUP
                    );
                    generatedGroups.add(subgroup);
                    addMemberships(subgroup, subgroups.get(index), generatedMemberships);
                }
            }
        }

        generatedGroups = teachingGroupRepository.saveAll(generatedGroups);
        teachingGroupRepository.flush();
        membershipRepository.saveAll(generatedMemberships);
        membershipRepository.flush();
        return generatedGroups;
    }

    private Integer findSubgroupCapacity(UUID semesterId) {
        return componentRepository.findBySubjectModuleSemesterIdAndAudienceMode(
                semesterId,
                TeachingAudienceMode.SUBGROUP
            )
            .stream()
            .map(ModuleTeachingComponent::getMaximumGroupSize)
            .filter(size -> size != null)
            .min(Integer::compareTo)
            .orElse(null);
    }

    private TeachingGroup createGroup(
        Semester semester,
        ClassGroup sourceClassGroup,
        String name,
        TeachingAudienceMode audienceType
    ) {
        TeachingGroup teachingGroup = new TeachingGroup();
        teachingGroup.setSemester(semester);
        teachingGroup.setSourceClassGroup(sourceClassGroup);
        teachingGroup.setName(name);
        teachingGroup.setAudienceType(audienceType);
        return teachingGroup;
    }

    private void addMemberships(
        TeachingGroup teachingGroup,
        List<SemesterRegestration> registrations,
        List<TeachingGroupMembership> memberships
    ) {
        for (SemesterRegestration registration : registrations) {
            TeachingGroupMembership membership = new TeachingGroupMembership();
            membership.setTeachingGroup(teachingGroup);
            membership.setSemesterRegistration(registration);
            memberships.add(membership);
        }
    }

    private List<List<SemesterRegestration>> splitBalanced(
        List<SemesterRegestration> registrations,
        int maximumSize
    ) {
        int groupCount = (registrations.size() + maximumSize - 1) / maximumSize;
        int baseSize = registrations.size() / groupCount;
        int largerGroups = registrations.size() % groupCount;
        List<List<SemesterRegestration>> groups = new ArrayList<>();
        int offset = 0;
        for (int index = 0; index < groupCount; index++) {
            int groupSize = baseSize + (index < largerGroups ? 1 : 0);
            groups.add(new ArrayList<>(registrations.subList(offset, offset + groupSize)));
            offset += groupSize;
        }
        return groups;
    }

    private void clearExistingGroups(UUID semesterId) {
        List<TeachingGroup> existingGroups =
            teachingGroupRepository.findBySemesterIdOrderByAudienceTypeAscNameAsc(semesterId);
        if (existingGroups.isEmpty()) {
            return;
        }
        List<UUID> groupIds = existingGroups.stream().map(TeachingGroup::getId).toList();
        membershipRepository.deleteAll(membershipRepository.findByTeachingGroupIdIn(groupIds));
        membershipRepository.flush();
        teachingGroupRepository.deleteAll(existingGroups);
        teachingGroupRepository.flush();
    }
}
