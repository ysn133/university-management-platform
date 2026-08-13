package com.platform.scheduling.examgroup.application;

import com.platform.academicregistration.classassignment.domain.StudentClassAssignment;
import com.platform.academicregistration.classassignment.infrastructure.StudentClassAssignmentRepository;
import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.identityaccess.infrastructure.UserProfileRepository;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.examgroup.domain.ExamGroup;
import com.platform.scheduling.examgroup.domain.ExamGroupMembership;
import com.platform.scheduling.examgroup.infrastructure.ExamGroupMembershipRepository;
import com.platform.scheduling.examgroup.infrastructure.ExamGroupRepository;
import com.platform.scheduling.examgroup.presentation.dto.ExamGroupPlanResponse;
import com.platform.scheduling.examgroup.presentation.dto.ExamGroupResponse;
import com.platform.scheduling.examgroup.presentation.dto.ExamGroupMemberResponse;
import com.platform.scheduling.examschedule.domain.ExamSchedule;
import com.platform.scheduling.examschedule.domain.PublicationStatus;
import com.platform.scheduling.examschedule.infrastructure.ExamScheduleRepository;
import com.platform.universitygovernance.classgroup.domain.ClassGroup;
import com.platform.universitygovernance.classgroup.infrastructure.ClassGroupRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ExamGroupService {
    private final ExamScheduleRepository scheduleRepository;
    private final ClassGroupRepository classGroupRepository;
    private final StudentClassAssignmentRepository classAssignmentRepository;
    private final ExamGroupRepository groupRepository;
    private final ExamGroupMembershipRepository membershipRepository;
    private final AdminPermissionAuthorizationService authorizationService;
    private final UserProfileRepository userProfileRepository;

    public ExamGroupService(ExamScheduleRepository scheduleRepository, ClassGroupRepository classGroupRepository,
        StudentClassAssignmentRepository classAssignmentRepository, ExamGroupRepository groupRepository,
        ExamGroupMembershipRepository membershipRepository, AdminPermissionAuthorizationService authorizationService,
        UserProfileRepository userProfileRepository) {
        this.scheduleRepository = scheduleRepository;
        this.classGroupRepository = classGroupRepository;
        this.classAssignmentRepository = classAssignmentRepository;
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.authorizationService = authorizationService;
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional(readOnly = true)
    public ExamGroupPlanResponse getPlan(AuthenticatedUserPrincipal principal, UUID scheduleId, UUID classGroupId) {
        ExamSchedule schedule = findSchedule(scheduleId);
        authorizationService.requirePermission(principal, schedule.getEstablishment().getId(), PermissionCode.EXAM_SCHEDULE_VIEW);
        ensureClassContext(schedule, findClassGroup(classGroupId));
        return response(scheduleId, classGroupId);
    }

    @Transactional
    public ExamGroupPlanResponse generate(AuthenticatedUserPrincipal principal, UUID scheduleId, UUID classGroupId, int splitCount) {
        ExamSchedule schedule = findSchedule(scheduleId);
        authorizationService.requirePermission(principal, schedule.getEstablishment().getId(), PermissionCode.EXAM_SCHEDULE_UPDATE);
        if (schedule.getPublicationStatus() == PublicationStatus.PUBLISHED) throw new ResponseStatusException(HttpStatus.CONFLICT, "Published exam groups cannot be changed");
        ClassGroup classGroup = findClassGroup(classGroupId);
        ensureClassContext(schedule, classGroup);
        List<StudentClassAssignment> students = new ArrayList<>(classAssignmentRepository
            .findBySemesterRegistrationSemesterIdAndClassGroupId(schedule.getSemester().getId(), classGroupId));
        students.sort(Comparator.comparing(item -> item.getSemesterRegistration().getAcademicRegistration().getStudent().getApogeeCode()));
        if (students.isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT, "The class group has no assigned students");
        if (splitCount > students.size()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Split count cannot exceed the number of students");

        groupRepository.deleteAll(groupRepository.findByExamScheduleIdAndClassGroupIdOrderByGroupOrderAsc(scheduleId, classGroupId));
        groupRepository.flush();
        List<ExamGroup> groups = new ArrayList<>();
        for (int index = 0; index < splitCount; index++) {
            ExamGroup group = new ExamGroup();
            group.setExamSchedule(schedule);
            group.setClassGroup(classGroup);
            group.setGroupOrder(index + 1);
            group.setLabel(splitCount == 1 ? classGroup.getName() : classGroup.getName() + "-E" + (index + 1));
            groups.add(groupRepository.save(group));
        }
        List<ExamGroupMembership> memberships = new ArrayList<>();
        for (int index = 0; index < students.size(); index++) {
            ExamGroupMembership membership = new ExamGroupMembership();
            membership.setExamGroup(groups.get(index % splitCount));
            membership.setSemesterRegistration(students.get(index).getSemesterRegistration());
            memberships.add(membership);
        }
        membershipRepository.saveAll(memberships);
        return response(scheduleId, classGroupId);
    }

    private ExamGroupPlanResponse response(UUID scheduleId, UUID classGroupId) {
        List<ExamGroupResponse> groups = groupRepository.findByExamScheduleIdAndClassGroupIdOrderByGroupOrderAsc(scheduleId, classGroupId)
            .stream().map(group -> {
                List<ExamGroupMemberResponse> members = membershipRepository.findByExamGroupIdIn(List.of(group.getId()))
                    .stream().map(membership -> {
                        var student = membership.getSemesterRegistration().getAcademicRegistration().getStudent();
                        var profile = userProfileRepository.findByUserAccountId(student.getUserAccount().getId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Student profile not found"));
                        return new ExamGroupMemberResponse(student.getId(), student.getApogeeCode(), student.getNationalStudentCode(),
                            profile.getCin(), profile.getLastName(), profile.getFirstName());
                    }).sorted(Comparator.comparing(ExamGroupMemberResponse::lastName).thenComparing(ExamGroupMemberResponse::firstName)).toList();
                return new ExamGroupResponse(group.getId(), group.getLabel(), group.getGroupOrder(), members.size(), members);
            }).toList();
        ExamSchedule schedule = findSchedule(scheduleId);
        int total = classAssignmentRepository.findBySemesterRegistrationSemesterIdAndClassGroupId(schedule.getSemester().getId(), classGroupId).size();
        return new ExamGroupPlanResponse(scheduleId, classGroupId, total, groups.size(), groups);
    }

    private ExamSchedule findSchedule(UUID id) { return scheduleRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam schedule not found")); }
    private ClassGroup findClassGroup(UUID id) { return classGroupRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class group not found")); }
    private void ensureClassContext(ExamSchedule schedule, ClassGroup group) {
        if (!group.getAcademicYear().getId().equals(schedule.getAcademicYear().getId()) || !group.getAcademicLevel().getId().equals(schedule.getSemester().getAcademicLevel().getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Class group does not belong to the exam schedule context");
    }
}
