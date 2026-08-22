package com.platform.teachingassignment.application;

import com.platform.academicregistration.moduleregistration.domain.ModuleRegistration;
import com.platform.academicregistration.moduleregistration.domain.ModuleRegistrationStatus;
import com.platform.academicregistration.moduleregistration.infrastructure.ModuleRegistrationRepository;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupMembershipRepository;
import com.platform.teachingassignment.domain.TeachingAssignment;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TeachingAssignmentRosterService {

    private final TeachingGroupMembershipRepository membershipRepository;
    private final ModuleRegistrationRepository moduleRegistrationRepository;

    public TeachingAssignmentRosterService(
        TeachingGroupMembershipRepository membershipRepository,
        ModuleRegistrationRepository moduleRegistrationRepository
    ) {
        this.membershipRepository = membershipRepository;
        this.moduleRegistrationRepository = moduleRegistrationRepository;
    }

    public List<ModuleRegistration> activeModuleRegistrations(
        TeachingAssignment assignment
    ) {
        var requirement = assignment.getTeachingRequirement();
        var teachingGroupId = requirement.getTeachingGroup().getId();
        var subjectModuleId = requirement.getModuleTeachingComponent()
            .getSubjectModule().getId();

        return membershipRepository.findByTeachingGroupId(teachingGroupId).stream()
            .flatMap(membership -> moduleRegistrationRepository
                .findBySemesterRegistrationIdAndStatus(
                    membership.getSemesterRegistration().getId(),
                    ModuleRegistrationStatus.ACTIVE
                )
                .stream())
            .filter(registration -> registration.getSubjectModule().getId()
                .equals(subjectModuleId))
            .toList();
    }
}
