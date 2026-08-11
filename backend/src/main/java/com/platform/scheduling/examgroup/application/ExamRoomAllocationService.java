package com.platform.scheduling.examgroup.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.examgroup.domain.ExamGroup;
import com.platform.scheduling.examgroup.domain.ExamRoomAllocation;
import com.platform.scheduling.examgroup.infrastructure.ExamGroupMembershipRepository;
import com.platform.scheduling.examgroup.infrastructure.ExamGroupRepository;
import com.platform.scheduling.examgroup.infrastructure.ExamRoomAllocationRepository;
import com.platform.scheduling.examgroup.presentation.dto.ExamRoomAllocationItemRequest;
import com.platform.scheduling.examgroup.presentation.dto.ExamRoomAllocationResponse;
import com.platform.scheduling.examschedule.domain.PublicationStatus;
import com.platform.scheduling.moduleexam.domain.ModuleExam;
import com.platform.scheduling.moduleexam.infrastructure.ModuleExamRepository;
import com.platform.scheduling.semesterschedule.infrastructure.ScheduleEntryRepository;
import com.platform.universitygovernance.room.domain.Room;
import com.platform.universitygovernance.room.domain.RoomStatus;
import com.platform.universitygovernance.room.infrastructure.RoomRepository;
import java.time.LocalTime;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ExamRoomAllocationService {
    private final ModuleExamRepository moduleExamRepository;
    private final ExamGroupRepository groupRepository;
    private final ExamGroupMembershipRepository membershipRepository;
    private final ExamRoomAllocationRepository allocationRepository;
    private final RoomRepository roomRepository;
    private final ScheduleEntryRepository scheduleEntryRepository;
    private final AdminPermissionAuthorizationService authorizationService;

    public ExamRoomAllocationService(ModuleExamRepository moduleExamRepository, ExamGroupRepository groupRepository,
        ExamGroupMembershipRepository membershipRepository, ExamRoomAllocationRepository allocationRepository,
        RoomRepository roomRepository, ScheduleEntryRepository scheduleEntryRepository,
        AdminPermissionAuthorizationService authorizationService) {
        this.moduleExamRepository = moduleExamRepository; this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository; this.allocationRepository = allocationRepository;
        this.roomRepository = roomRepository; this.scheduleEntryRepository = scheduleEntryRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional(readOnly = true)
    public List<ExamRoomAllocationResponse> get(AuthenticatedUserPrincipal principal, UUID moduleExamId) {
        ModuleExam exam = findExam(moduleExamId);
        authorizationService.requirePermission(principal, exam.getExamSchedule().getEstablishment().getId(), PermissionCode.EXAM_SCHEDULE_VIEW);
        return allocationRepository.findByModuleExamIdOrderByExamGroupGroupOrderAsc(moduleExamId).stream().map(this::response).toList();
    }

    @Transactional
    public List<ExamRoomAllocationResponse> replace(AuthenticatedUserPrincipal principal, UUID moduleExamId, List<ExamRoomAllocationItemRequest> items) {
        ModuleExam exam = findExam(moduleExamId);
        authorizationService.requirePermission(principal, exam.getExamSchedule().getEstablishment().getId(), PermissionCode.EXAM_SCHEDULE_UPDATE);
        if (exam.getExamSchedule().getPublicationStatus() == PublicationStatus.PUBLISHED) throw new ResponseStatusException(HttpStatus.CONFLICT, "Published exam allocations cannot be changed");
        List<ExamGroup> groups = groupRepository.findByExamScheduleIdAndClassGroupIdOrderByGroupOrderAsc(exam.getExamSchedule().getId(), exam.getClassGroup().getId());
        if (groups.isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Generate exam groups for this class group first");
        Set<UUID> expected = groups.stream().map(ExamGroup::getId).collect(java.util.stream.Collectors.toSet());
        Set<UUID> suppliedGroups = new HashSet<>();
        Set<UUID> suppliedRooms = new HashSet<>();
        List<ExamRoomAllocation> allocations = new ArrayList<>();
        for (ExamRoomAllocationItemRequest item : items) {
            if (!expected.contains(item.examGroupId()) || !suppliedGroups.add(item.examGroupId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allocate exactly one room to every exam group");
            if (!suppliedRooms.add(item.roomId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each exam group must use a different room");
            ExamGroup group = groups.stream().filter(value -> value.getId().equals(item.examGroupId())).findFirst().orElseThrow();
            Room room = roomRepository.findById(item.roomId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
            validateRoom(exam, group, room);
            ExamRoomAllocation allocation = new ExamRoomAllocation(); allocation.setModuleExam(exam); allocation.setExamGroup(group); allocation.setRoom(room); allocations.add(allocation);
        }
        if (!suppliedGroups.equals(expected)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Allocate exactly one room to every exam group");
        allocationRepository.deleteByModuleExamId(moduleExamId);
        allocationRepository.flush();
        return allocationRepository.saveAll(allocations).stream().map(this::response).toList();
    }

    private void validateRoom(ModuleExam exam, ExamGroup group, Room room) {
        if (room.getStatus() != RoomStatus.ACTIVE || !room.getEstablishment().getId().equals(exam.getExamSchedule().getEstablishment().getId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room must be active and belong to the establishment");
        long students = membershipRepository.countByExamGroupId(group.getId());
        if (room.getCapacity() < students) throw new ResponseStatusException(HttpStatus.CONFLICT, "Room capacity is smaller than the exam group");
        boolean examConflict = allocationRepository.findByRoomId(room.getId()).stream().filter(value -> !value.getModuleExam().getId().equals(exam.getId()))
            .anyMatch(value -> value.getModuleExam().getExamDate().equals(exam.getExamDate()) && overlaps(exam.getStartTime(), exam.getEndTime(), value.getModuleExam().getStartTime(), value.getModuleExam().getEndTime()));
        if (examConflict) throw new ResponseStatusException(HttpStatus.CONFLICT, "Room already has an overlapping exam");
        boolean teachingConflict = scheduleEntryRepository.findByRoomIdAndDayOfWeek(room.getId(), exam.getExamDate().getDayOfWeek()).stream()
            .filter(entry -> !exam.getExamDate().isBefore(entry.getSemesterSchedule().getSemester().getStartDate()) && !exam.getExamDate().isAfter(entry.getSemesterSchedule().getSemester().getEndDate()))
            .anyMatch(entry -> overlaps(exam.getStartTime(), exam.getEndTime(), entry.getStartTime(), entry.getEndTime()));
        if (teachingConflict) throw new ResponseStatusException(HttpStatus.CONFLICT, "Room is occupied by an active teaching session");
    }

    private boolean overlaps(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) { return aStart.equals(bStart) || (aEnd != null && bEnd != null && aStart.isBefore(bEnd) && aEnd.isAfter(bStart)); }
    private ModuleExam findExam(UUID id) { return moduleExamRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module exam not found")); }
    private ExamRoomAllocationResponse response(ExamRoomAllocation value) { Room room = value.getRoom(); ExamGroup group = value.getExamGroup(); return new ExamRoomAllocationResponse(value.getId(), group.getId(), group.getLabel(), membershipRepository.countByExamGroupId(group.getId()), room.getId(), room.getCode(), room.getName(), room.getCapacity()); }
}
