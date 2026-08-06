package com.platform.scheduling.teachinggroup.application;

import com.platform.academicregistration.classassignment.domain.StudentClassAssignment;
import com.platform.academicregistration.classassignment.infrastructure.StudentClassAssignmentRepository;
import com.platform.academicregistration.registration.domain.AcademicRegistrationStatus;
import com.platform.academicregistration.semesterregistration.domain.SemesterRegistration;
import com.platform.academicregistration.semesterregistration.infrastructure.SemesterRegistrationRepository;
import com.platform.scheduling.teachinggroup.domain.TeachingGroup;
import com.platform.scheduling.teachinggroup.domain.TeachingGroupMembership;
import com.platform.scheduling.teachinggroup.domain.TeachingGroupPolicy;
import com.platform.scheduling.teachinggroup.domain.TeachingGroupType;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupMembershipRepository;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupPolicyRepository;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupRepository;
import com.platform.teachingrequirement.infrastructure.TeachingRequirementRepository;
import com.platform.universitygovernance.classgroup.domain.ClassGroup;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import com.platform.universitygovernance.moduleteachingcomponent.infrastructure.ModuleTeachingComponentRepository;
import com.platform.universitygovernance.semester.domain.Semester;
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TeachingGroupGenerationService {

    private final SemesterRepository semesterRepository;
    private final SemesterRegistrationRepository semesterRegistrationRepository;
    private final StudentClassAssignmentRepository classAssignmentRepository;
    private final ModuleTeachingComponentRepository componentRepository;
    private final TeachingGroupPolicyRepository policyRepository;
    private final TeachingGroupRepository teachingGroupRepository;
    private final TeachingGroupMembershipRepository membershipRepository;
    private final TeachingRequirementRepository requirementRepository;

    public TeachingGroupGenerationService(
        SemesterRepository semesterRepository,
        SemesterRegistrationRepository semesterRegistrationRepository,
        StudentClassAssignmentRepository classAssignmentRepository,
        ModuleTeachingComponentRepository componentRepository,
        TeachingGroupPolicyRepository policyRepository,
        TeachingGroupRepository teachingGroupRepository,
        TeachingGroupMembershipRepository membershipRepository,
        TeachingRequirementRepository requirementRepository
    ) {
        this.semesterRepository = semesterRepository;
        this.semesterRegistrationRepository = semesterRegistrationRepository;
        this.classAssignmentRepository = classAssignmentRepository;
        this.componentRepository = componentRepository;
        this.policyRepository = policyRepository;
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

        List<SemesterRegistration> activeRegistrations = semesterRegistrationRepository
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
        for (SemesterRegistration registration : activeRegistrations) {
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

        Map<ClassGroup, List<SemesterRegistration>> registrationsByClass = new LinkedHashMap<>();
        activeRegistrations.stream()
            .map(registration -> assignmentsByRegistration.get(registration.getId()).getClassGroup())
            .distinct()
            .sorted(Comparator.comparing(ClassGroup::getName).thenComparing(ClassGroup::getId))
            .forEach(classGroup -> registrationsByClass.put(classGroup, new ArrayList<>()));
        for (SemesterRegistration registration : activeRegistrations) {
            ClassGroup classGroup = assignmentsByRegistration
                .get(registration.getId())
                .getClassGroup();
            registrationsByClass.get(classGroup).add(registration);
        }

        Set<TeachingComponentType> requiredSubgroupTypes = componentRepository
            .findBySubjectModuleSemesterIdAndAudienceMode(
                semesterId,
                TeachingAudienceMode.SUBGROUP
            )
            .stream()
            .map(component -> component.getComponentType())
            .collect(Collectors.toSet());
        Map<TeachingGroupType, TeachingGroupPolicy> policiesByType =
            new EnumMap<>(TeachingGroupType.class);
        policyRepository
            .findByAcademicLevelIdAndAcademicYearIdOrderByGroupTypeAsc(
                semester.getAcademicLevel().getId(),
                semester.getAcademicYear().getId()
            )
            .forEach(policy -> policiesByType.put(policy.getGroupType(), policy));
        for (TeachingComponentType requiredType : requiredSubgroupTypes) {
            TeachingGroupType groupType = TeachingGroupType.valueOf(requiredType.name());
            if (!policiesByType.containsKey(groupType)) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A teaching group policy is required for " + requiredType
                );
            }
        }
        List<TeachingGroupPolicy> subgroupPolicies = requiredSubgroupTypes.stream()
            .map(type -> policiesByType.get(TeachingGroupType.valueOf(type.name())))
            .sorted(Comparator.comparing(TeachingGroupPolicy::getGroupType))
            .toList();
        List<TeachingGroup> generatedGroups = new ArrayList<>();
        List<TeachingGroupMembership> generatedMemberships = new ArrayList<>();

        TeachingGroup cohort = createGroup(
            semester,
            null,
            "Whole Cohort",
            TeachingAudienceMode.WHOLE_COHORT,
            null
        );
        generatedGroups.add(cohort);
        addMemberships(cohort, activeRegistrations, generatedMemberships);

        for (Map.Entry<ClassGroup, List<SemesterRegistration>> entry :
            registrationsByClass.entrySet()) {
            ClassGroup classGroup = entry.getKey();
            List<SemesterRegistration> classRegistrations = entry.getValue();

            TeachingGroup classAudience = createGroup(
                semester,
                classGroup,
                classGroup.getName(),
                TeachingAudienceMode.CLASS_GROUP,
                null
            );
            generatedGroups.add(classAudience);
            addMemberships(classAudience, classRegistrations, generatedMemberships);

            for (TeachingGroupPolicy policy : subgroupPolicies) {
                List<List<SemesterRegistration>> subgroups = splitBalanced(
                    classRegistrations,
                    policy.getMaximumGroupSize()
                );
                for (int index = 0; index < subgroups.size(); index++) {
                    TeachingGroup subgroup = createGroup(
                        semester,
                        classGroup,
                        classGroup.getName() + " " + policy.getGroupType() + (index + 1),
                        TeachingAudienceMode.SUBGROUP,
                        policy.getGroupType()
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

    private TeachingGroup createGroup(
        Semester semester,
        ClassGroup sourceClassGroup,
        String name,
        TeachingAudienceMode audienceType,
        TeachingGroupType groupType
    ) {
        TeachingGroup teachingGroup = new TeachingGroup();
        teachingGroup.setSemester(semester);
        teachingGroup.setSourceClassGroup(sourceClassGroup);
        teachingGroup.setName(name);
        teachingGroup.setAudienceType(audienceType);
        teachingGroup.setGroupType(groupType);
        return teachingGroup;
    }

    private void addMemberships(
        TeachingGroup teachingGroup,
        List<SemesterRegistration> registrations,
        List<TeachingGroupMembership> memberships
    ) {
        for (SemesterRegistration registration : registrations) {
            TeachingGroupMembership membership = new TeachingGroupMembership();
            membership.setTeachingGroup(teachingGroup);
            membership.setSemesterRegistration(registration);
            memberships.add(membership);
        }
    }

    private List<List<SemesterRegistration>> splitBalanced(
        List<SemesterRegistration> registrations,
        int maximumSize
    ) {
        int groupCount = (registrations.size() + maximumSize - 1) / maximumSize;
        int baseSize = registrations.size() / groupCount;
        int largerGroups = registrations.size() % groupCount;
        List<List<SemesterRegistration>> groups = new ArrayList<>();
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
