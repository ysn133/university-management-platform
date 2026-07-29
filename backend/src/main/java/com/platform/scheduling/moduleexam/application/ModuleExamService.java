package com.platform.scheduling.moduleexam.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.examschedule.domain.ExamSchedule;
import com.platform.scheduling.examschedule.domain.PublicationStatus;
import com.platform.scheduling.examschedule.infrastructure.ExamScheduleRepository;
import com.platform.scheduling.moduleexam.domain.ModuleExam;
import com.platform.scheduling.moduleexam.infrastructure.ModuleExamRepository;
import com.platform.scheduling.moduleexam.presentation.dto.CreateModuleExamRequest;
import com.platform.scheduling.moduleexam.presentation.dto.ModuleExamResponse;
import com.platform.scheduling.moduleexam.presentation.dto.UpdateModuleExamRequest;
import com.platform.shared.presentation.ActionResponse;
import com.platform.teachingassignment.domain.TeachingAssignment;
import com.platform.teachingassignment.domain.TeachingAssignmentStatus;
import com.platform.teachingassignment.infrastructure.TeachingAssignmentRepository;
import com.platform.universitygovernance.classgroup.domain.ClassGroup;
import com.platform.universitygovernance.classgroup.domain.ClassGroupStatus;
import com.platform.universitygovernance.classgroup.infrastructure.ClassGroupRepository;
import com.platform.universitygovernance.subjectmodules.domain.SubjectModule;
import com.platform.universitygovernance.subjectmodules.infrastructure.SubjectModuleRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ModuleExamService {

    private final ModuleExamRepository moduleExamRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final SubjectModuleRepository subjectModuleRepository;
    private final ClassGroupRepository classGroupRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public ModuleExamService(
        ModuleExamRepository moduleExamRepository,
        ExamScheduleRepository examScheduleRepository,
        SubjectModuleRepository subjectModuleRepository,
        ClassGroupRepository classGroupRepository,
        TeachingAssignmentRepository teachingAssignmentRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.moduleExamRepository = moduleExamRepository;
        this.examScheduleRepository = examScheduleRepository;
        this.subjectModuleRepository = subjectModuleRepository;
        this.classGroupRepository = classGroupRepository;
        this.teachingAssignmentRepository = teachingAssignmentRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public ModuleExamResponse createModuleExam(
        AuthenticatedUserPrincipal principal,
        UUID examScheduleId,
        CreateModuleExamRequest request
    ) {
        ExamSchedule examSchedule = findExamSchedule(examScheduleId);
        requireUpdatePermission(principal, examSchedule);
        ensureDraft(examSchedule);

        SubjectModule subjectModule = findSubjectModule(request.subjectModuleId());
        ClassGroup classGroup = findClassGroup(request.classGroupId());
        TeachingAssignment teachingAssignment = findTeachingAssignment(
            request.teachingAssignmentId()
        );
        ensureExamContext(
            examSchedule,
            subjectModule,
            classGroup,
            teachingAssignment
        );
        ensureValidTimeRange(request.startTime(), request.endTime());
        ensureUniqueModuleExam(
            examScheduleId,
            subjectModule.getId(),
            classGroup.getId(),
            null
        );
        ensureNoGroupConflict(
            examScheduleId,
            classGroup.getId(),
            request.examDate(),
            request.startTime(),
            request.endTime(),
            null
        );

        ModuleExam moduleExam = new ModuleExam();
        moduleExam.setExamSchedule(examSchedule);
        applyValues(
            moduleExam,
            subjectModule,
            classGroup,
            teachingAssignment,
            request.examDate(),
            request.startTime(),
            request.endTime(),
            request.location()
        );
        return toResponse(moduleExamRepository.save(moduleExam));
    }

    @Transactional(readOnly = true)
    public List<ModuleExamResponse> getModuleExams(
        AuthenticatedUserPrincipal principal,
        UUID examScheduleId
    ) {
        ExamSchedule examSchedule = findExamSchedule(examScheduleId);
        requireViewPermission(principal, examSchedule);

        return moduleExamRepository
            .findByExamScheduleIdOrderByExamDateAscStartTimeAsc(examScheduleId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ModuleExamResponse getModuleExam(
        AuthenticatedUserPrincipal principal,
        UUID moduleExamId
    ) {
        ModuleExam moduleExam = findModuleExam(moduleExamId);
        requireViewPermission(principal, moduleExam.getExamSchedule());
        return toResponse(moduleExam);
    }

    @Transactional
    public ModuleExamResponse updateModuleExam(
        AuthenticatedUserPrincipal principal,
        UUID moduleExamId,
        UpdateModuleExamRequest request
    ) {
        ModuleExam moduleExam = findModuleExam(moduleExamId);
        ExamSchedule examSchedule = moduleExam.getExamSchedule();
        requireUpdatePermission(principal, examSchedule);
        ensureDraft(examSchedule);

        SubjectModule subjectModule = findSubjectModule(request.subjectModuleId());
        ClassGroup classGroup = findClassGroup(request.classGroupId());
        TeachingAssignment teachingAssignment = findTeachingAssignment(
            request.teachingAssignmentId()
        );
        ensureExamContext(
            examSchedule,
            subjectModule,
            classGroup,
            teachingAssignment
        );
        ensureValidTimeRange(request.startTime(), request.endTime());
        ensureUniqueModuleExam(
            examSchedule.getId(),
            subjectModule.getId(),
            classGroup.getId(),
            moduleExamId
        );
        ensureNoGroupConflict(
            examSchedule.getId(),
            classGroup.getId(),
            request.examDate(),
            request.startTime(),
            request.endTime(),
            moduleExamId
        );

        applyValues(
            moduleExam,
            subjectModule,
            classGroup,
            teachingAssignment,
            request.examDate(),
            request.startTime(),
            request.endTime(),
            request.location()
        );
        return toResponse(moduleExamRepository.save(moduleExam));
    }

    @Transactional
    public ActionResponse deleteModuleExam(
        AuthenticatedUserPrincipal principal,
        UUID moduleExamId
    ) {
        ModuleExam moduleExam = findModuleExam(moduleExamId);
        requireUpdatePermission(principal, moduleExam.getExamSchedule());
        ensureDraft(moduleExam.getExamSchedule());
        moduleExamRepository.delete(moduleExam);
        return new ActionResponse(true, "Module exam deleted");
    }

    private ExamSchedule findExamSchedule(UUID examScheduleId) {
        return examScheduleRepository.findById(examScheduleId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Exam schedule not found"
            ));
    }

    private ModuleExam findModuleExam(UUID moduleExamId) {
        return moduleExamRepository.findById(moduleExamId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Module exam not found"
            ));
    }

    private SubjectModule findSubjectModule(UUID subjectModuleId) {
        return subjectModuleRepository.findById(subjectModuleId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Subject module not found"
            ));
    }

    private ClassGroup findClassGroup(UUID classGroupId) {
        return classGroupRepository.findById(classGroupId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Class group not found"
            ));
    }

    private TeachingAssignment findTeachingAssignment(UUID teachingAssignmentId) {
        if (teachingAssignmentId == null) {
            return null;
        }
        return teachingAssignmentRepository.findById(teachingAssignmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Teaching assignment not found"
            ));
    }

    private void ensureExamContext(
        ExamSchedule examSchedule,
        SubjectModule subjectModule,
        ClassGroup classGroup,
        TeachingAssignment teachingAssignment
    ) {
        boolean compatible = examSchedule.getSemester().getId().equals(
            subjectModule.getSemester().getId()
        )
            && examSchedule.getAcademicYear().getId().equals(
                classGroup.getAcademicYear().getId()
            )
            && examSchedule.getSemester().getAcademicLevel().getId().equals(
                classGroup.getAcademicLevel().getId()
            )
            && classGroup.getStatus() == ClassGroupStatus.ACTIVE;

        if (!compatible) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Subject module and class group must match the exam schedule context"
            );
        }

        if (teachingAssignment != null) {
            ensureTeachingAssignmentMatches(
                examSchedule,
                subjectModule,
                classGroup,
                teachingAssignment
            );
        }
    }

    private void ensureTeachingAssignmentMatches(
        ExamSchedule examSchedule,
        SubjectModule subjectModule,
        ClassGroup classGroup,
        TeachingAssignment teachingAssignment
    ) {
        boolean compatible = teachingAssignment.getStatus()
                == TeachingAssignmentStatus.ACTIVE
            && teachingAssignment.getSubjectModule().getId().equals(subjectModule.getId())
            && teachingAssignment.getClassGroup().getId().equals(classGroup.getId())
            && teachingAssignment.getAcademicYear().getId().equals(
                examSchedule.getAcademicYear().getId()
            )
            && teachingAssignment.getSemester().getId().equals(
                examSchedule.getSemester().getId()
            )
            && teachingAssignment.getProfessor().getEstablishment().getId().equals(
                examSchedule.getEstablishment().getId()
            );

        if (!compatible) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Teaching assignment does not match the module exam context"
            );
        }
    }

    private void ensureValidTimeRange(LocalTime startTime, LocalTime endTime) {
        if (endTime != null && !endTime.isAfter(startTime)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "End time must be after start time"
            );
        }
    }

    private void ensureUniqueModuleExam(
        UUID examScheduleId,
        UUID subjectModuleId,
        UUID classGroupId,
        UUID excludedModuleExamId
    ) {
        boolean exists = excludedModuleExamId == null
            ? moduleExamRepository
                .existsByExamScheduleIdAndSubjectModuleIdAndClassGroupId(
                    examScheduleId,
                    subjectModuleId,
                    classGroupId
                )
            : moduleExamRepository
                .existsByExamScheduleIdAndSubjectModuleIdAndClassGroupIdAndIdNot(
                    examScheduleId,
                    subjectModuleId,
                    classGroupId,
                    excludedModuleExamId
                );

        if (exists) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A module exam already exists for this subject module and class group"
            );
        }
    }

    private void ensureNoGroupConflict(
        UUID examScheduleId,
        UUID classGroupId,
        LocalDate examDate,
        LocalTime startTime,
        LocalTime endTime,
        UUID excludedModuleExamId
    ) {
        boolean conflict = moduleExamRepository
            .findByExamScheduleIdAndClassGroupIdAndExamDate(
                examScheduleId,
                classGroupId,
                examDate
            )
            .stream()
            .filter(exam -> excludedModuleExamId == null
                || !excludedModuleExamId.equals(exam.getId()))
            .anyMatch(exam -> overlaps(
                startTime,
                endTime,
                exam.getStartTime(),
                exam.getEndTime()
            ));

        if (conflict) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The class group already has an overlapping module exam"
            );
        }
    }

    private boolean overlaps(
        LocalTime firstStart,
        LocalTime firstEnd,
        LocalTime secondStart,
        LocalTime secondEnd
    ) {
        if (firstStart.equals(secondStart)) {
            return true;
        }
        if (firstEnd == null || secondEnd == null) {
            return false;
        }
        return firstStart.isBefore(secondEnd) && firstEnd.isAfter(secondStart);
    }

    private void requireViewPermission(
        AuthenticatedUserPrincipal principal,
        ExamSchedule examSchedule
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            examSchedule.getEstablishment().getId(),
            PermissionCode.EXAM_SCHEDULE_VIEW
        );
    }

    private void requireUpdatePermission(
        AuthenticatedUserPrincipal principal,
        ExamSchedule examSchedule
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            examSchedule.getEstablishment().getId(),
            PermissionCode.EXAM_SCHEDULE_UPDATE
        );
    }

    private void ensureDraft(ExamSchedule examSchedule) {
        if (examSchedule.getPublicationStatus() != PublicationStatus.DRAFT) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Published exam schedules cannot be modified"
            );
        }
    }

    private void applyValues(
        ModuleExam moduleExam,
        SubjectModule subjectModule,
        ClassGroup classGroup,
        TeachingAssignment teachingAssignment,
        LocalDate examDate,
        LocalTime startTime,
        LocalTime endTime,
        String location
    ) {
        moduleExam.setSubjectModule(subjectModule);
        moduleExam.setClassGroup(classGroup);
        moduleExam.setTeachingAssignment(teachingAssignment);
        moduleExam.setExamDate(examDate);
        moduleExam.setStartTime(startTime);
        moduleExam.setEndTime(endTime);
        moduleExam.setLocation(normalizeLocation(location));
    }

    private String normalizeLocation(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }
        return location.trim();
    }

    private ModuleExamResponse toResponse(ModuleExam moduleExam) {
        TeachingAssignment teachingAssignment = moduleExam.getTeachingAssignment();
        return new ModuleExamResponse(
            moduleExam.getId(),
            moduleExam.getExamSchedule().getId(),
            moduleExam.getSubjectModule().getId(),
            moduleExam.getClassGroup().getId(),
            teachingAssignment == null ? null : teachingAssignment.getId(),
            moduleExam.getExamDate(),
            moduleExam.getStartTime(),
            moduleExam.getEndTime(),
            moduleExam.getLocation(),
            moduleExam.getCandidateListGeneratedAt(),
            moduleExam.getCreatedAt(),
            moduleExam.getUpdatedAt()
        );
    }
}
