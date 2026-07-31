package com.platform.scheduling.semesterschedule.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.semesterschedule.domain.ScheduleEntry;
import com.platform.scheduling.semesterschedule.domain.SchedulePublicationStatus;
import com.platform.scheduling.semesterschedule.domain.SemesterSchedule;
import com.platform.scheduling.semesterschedule.infrastructure.ScheduleEntryRepository;
import com.platform.scheduling.semesterschedule.infrastructure.SemesterScheduleRepository;
import com.platform.scheduling.semesterschedule.presentation.dto.CreateScheduleEntryRequest;
import com.platform.scheduling.semesterschedule.presentation.dto.ScheduleEntryResponse;
import com.platform.scheduling.semesterschedule.presentation.dto.UpdateScheduleEntryRequest;
import com.platform.scheduling.teachinggroup.domain.TeachingGroup;
import com.platform.scheduling.teachinggroup.infrastructure.TeachingGroupMembershipRepository;
import com.platform.shared.presentation.ActionResponse;
import com.platform.teachingassignment.domain.TeachingAssignment;
import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import com.platform.teachingassignment.infrastructure.TeachingAssignmentRepository;
import com.platform.teachingrequirement.domain.TeachingRequirement;
import com.platform.teachingrequirement.domain.TeachingRequirementStatus;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ScheduleEntryService {

    private final ScheduleEntryRepository scheduleEntryRepository;
    private final SemesterScheduleRepository semesterScheduleRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final TeachingGroupMembershipRepository membershipRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public ScheduleEntryService(
        ScheduleEntryRepository scheduleEntryRepository,
        SemesterScheduleRepository semesterScheduleRepository,
        TeachingAssignmentRepository teachingAssignmentRepository,
        TeachingGroupMembershipRepository membershipRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.scheduleEntryRepository = scheduleEntryRepository;
        this.semesterScheduleRepository = semesterScheduleRepository;
        this.teachingAssignmentRepository = teachingAssignmentRepository;
        this.membershipRepository = membershipRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public ScheduleEntryResponse createScheduleEntry(
        AuthenticatedUserPrincipal principal,
        UUID scheduleId,
        CreateScheduleEntryRequest request
    ) {
        SemesterSchedule schedule = findSchedule(scheduleId);
        requireUpdatePermission(principal, schedule);
        ensureDraft(schedule);

        TeachingAssignment assignment = findTeachingAssignment(
            request.teachingAssignmentId()
        );
        ensureAssignmentMatchesSchedule(assignment, schedule);
        ensureValidTimeRange(request.startTime(), request.endTime());
        ensureNoConflict(
            schedule,
            assignment,
            request.dayOfWeek(),
            request.startTime(),
            request.endTime(),
            null
        );

        ScheduleEntry entry = new ScheduleEntry();
        entry.setSemesterSchedule(schedule);
        applyEntryValues(
            entry,
            assignment,
            request.dayOfWeek(),
            request.startTime(),
            request.endTime(),
            request.location()
        );
        return toResponse(scheduleEntryRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public List<ScheduleEntryResponse> getScheduleEntries(
        AuthenticatedUserPrincipal principal,
        UUID scheduleId
    ) {
        SemesterSchedule schedule = findSchedule(scheduleId);
        requireViewPermission(principal, schedule);

        return scheduleEntryRepository
            .findBySemesterScheduleId(scheduleId)
            .stream()
            .sorted(
                Comparator.comparingInt(
                    (ScheduleEntry entry) -> entry.getDayOfWeek().getValue()
                ).thenComparing(ScheduleEntry::getStartTime)
            )
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ScheduleEntryResponse getScheduleEntry(
        AuthenticatedUserPrincipal principal,
        UUID scheduleEntryId
    ) {
        ScheduleEntry entry = findScheduleEntry(scheduleEntryId);
        requireViewPermission(principal, entry.getSemesterSchedule());
        return toResponse(entry);
    }

    @Transactional
    public ScheduleEntryResponse updateScheduleEntry(
        AuthenticatedUserPrincipal principal,
        UUID scheduleEntryId,
        UpdateScheduleEntryRequest request
    ) {
        ScheduleEntry entry = findScheduleEntry(scheduleEntryId);
        SemesterSchedule schedule = entry.getSemesterSchedule();
        requireUpdatePermission(principal, schedule);
        ensureDraft(schedule);

        TeachingAssignment assignment = findTeachingAssignment(
            request.teachingAssignmentId()
        );
        ensureAssignmentMatchesSchedule(assignment, schedule);
        ensureValidTimeRange(request.startTime(), request.endTime());
        ensureNoConflict(
            schedule,
            assignment,
            request.dayOfWeek(),
            request.startTime(),
            request.endTime(),
            entry.getId()
        );

        applyEntryValues(
            entry,
            assignment,
            request.dayOfWeek(),
            request.startTime(),
            request.endTime(),
            request.location()
        );
        return toResponse(scheduleEntryRepository.save(entry));
    }

    @Transactional
    public ActionResponse deleteScheduleEntry(
        AuthenticatedUserPrincipal principal,
        UUID scheduleEntryId
    ) {
        ScheduleEntry entry = findScheduleEntry(scheduleEntryId);
        requireUpdatePermission(principal, entry.getSemesterSchedule());
        ensureDraft(entry.getSemesterSchedule());
        scheduleEntryRepository.delete(entry);
        return new ActionResponse(true, "Schedule entry deleted");
    }

    private SemesterSchedule findSchedule(UUID scheduleId) {
        return semesterScheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Semester schedule not found"
            ));
    }

    private ScheduleEntry findScheduleEntry(UUID scheduleEntryId) {
        return scheduleEntryRepository.findById(scheduleEntryId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Schedule entry not found"
            ));
    }

    private TeachingAssignment findTeachingAssignment(UUID teachingAssignmentId) {
        return teachingAssignmentRepository.findById(teachingAssignmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Teaching assignment not found"
            ));
    }

    private void ensureAssignmentMatchesSchedule(
        TeachingAssignment assignment,
        SemesterSchedule schedule
    ) {
        UUID establishmentId = schedule.getEstablishment().getId();
        TeachingRequirement requirement = assignment.getTeachingRequirement();
        TeachingGroup teachingGroup = requirement.getTeachingGroup();
        UUID requirementEstablishmentId = teachingGroup
            .getSemester()
            .getAcademicLevel()
            .getProgramFiliere()
            .getDepartment()
            .getEstablishment()
            .getId();

        boolean compatible = assignment.getStatus() == TeachingAssignmentStatus.ACTIVE
            && requirement.getStatus() == TeachingRequirementStatus.ACTIVE
            && establishmentId.equals(assignment.getProfessor().getEstablishment().getId())
            && establishmentId.equals(requirementEstablishmentId)
            && schedule.getAcademicYear().getId().equals(
                teachingGroup.getSemester().getAcademicYear().getId()
            )
            && schedule.getSemester().getId().equals(teachingGroup.getSemester().getId())
            && teachingGroup.getSemester().getId().equals(
                requirement.getModuleTeachingComponent()
                    .getSubjectModule()
                    .getSemester()
                    .getId()
            );

        if (!compatible) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Teaching assignment does not match the schedule context"
            );
        }
    }

    private void ensureValidTimeRange(LocalTime startTime, LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "End time must be after start time"
            );
        }
    }

    private void ensureNoConflict(
        SemesterSchedule schedule,
        TeachingAssignment assignment,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        UUID excludedEntryId
    ) {
        List<ScheduleEntry> entries = scheduleEntryRepository
            .findPotentialConflicts(
                schedule.getEstablishment().getId(),
                schedule.getAcademicYear().getId(),
                schedule.getSemester().getSemesterOrder(),
                dayOfWeek
            );

        boolean conflict = entries.stream()
            .filter(entry -> excludedEntryId == null
                || !excludedEntryId.equals(entry.getId()))
            .filter(entry -> overlaps(
                startTime,
                endTime,
                entry.getStartTime(),
                entry.getEndTime()
            ))
            .anyMatch(entry ->
                entry.getTeachingAssignment().getProfessor().getId().equals(
                    assignment.getProfessor().getId()
                )
                || audiencesOverlap(entry.getTeachingAssignment(), assignment)
            );

        if (conflict) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The professor or class group already has an overlapping schedule entry"
            );
        }
    }

    private boolean audiencesOverlap(
        TeachingAssignment first,
        TeachingAssignment second
    ) {
        UUID firstGroupId = first.getTeachingRequirement().getTeachingGroup().getId();
        UUID secondGroupId = second.getTeachingRequirement().getTeachingGroup().getId();
        if (firstGroupId.equals(secondGroupId)) {
            return true;
        }
        Set<UUID> firstMembers = membershipRepository
            .findByTeachingGroupId(firstGroupId)
            .stream()
            .map(membership -> membership.getSemesterRegistration().getId())
            .collect(Collectors.toSet());
        return membershipRepository
            .findByTeachingGroupId(secondGroupId)
            .stream()
            .map(membership -> membership.getSemesterRegistration().getId())
            .anyMatch(firstMembers::contains);
    }

    private boolean overlaps(
        LocalTime firstStart,
        LocalTime firstEnd,
        LocalTime secondStart,
        LocalTime secondEnd
    ) {
        return firstStart.isBefore(secondEnd) && firstEnd.isAfter(secondStart);
    }

    private void ensureDraft(SemesterSchedule schedule) {
        if (schedule.getPublicationStatus() != SchedulePublicationStatus.DRAFT) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Published schedules cannot be modified"
            );
        }
    }

    private void requireViewPermission(
        AuthenticatedUserPrincipal principal,
        SemesterSchedule schedule
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            schedule.getEstablishment().getId(),
            PermissionCode.SEMESTER_SCHEDULE_VIEW
        );
    }

    private void requireUpdatePermission(
        AuthenticatedUserPrincipal principal,
        SemesterSchedule schedule
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            schedule.getEstablishment().getId(),
            PermissionCode.SEMESTER_SCHEDULE_UPDATE
        );
    }

    private void applyEntryValues(
        ScheduleEntry entry,
        TeachingAssignment assignment,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        String location
    ) {
        entry.setTeachingAssignment(assignment);
        entry.setDayOfWeek(dayOfWeek);
        entry.setStartTime(startTime);
        entry.setEndTime(endTime);
        entry.setLocation(normalizeLocation(location));
    }

    private String normalizeLocation(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }
        return location.trim();
    }

    private ScheduleEntryResponse toResponse(ScheduleEntry entry) {
        TeachingAssignment assignment = entry.getTeachingAssignment();
        TeachingRequirement requirement = assignment.getTeachingRequirement();
        return new ScheduleEntryResponse(
            entry.getId(),
            entry.getSemesterSchedule().getId(),
            assignment.getId(),
            assignment.getProfessor().getId(),
            requirement.getModuleTeachingComponent().getSubjectModule().getId(),
            requirement.getTeachingGroup().getId(),
            requirement.getTeachingGroup().getName(),
            entry.getDayOfWeek(),
            entry.getStartTime(),
            entry.getEndTime(),
            entry.getLocation(),
            entry.getCreatedAt(),
            entry.getUpdatedAt()
        );
    }
}
