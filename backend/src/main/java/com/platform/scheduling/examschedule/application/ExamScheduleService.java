package com.platform.scheduling.examschedule.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.examschedule.domain.ExamSchedule;
import com.platform.scheduling.examschedule.domain.PublicationStatus;
import com.platform.scheduling.examschedule.infrastructure.ExamScheduleRepository;
import com.platform.scheduling.examgroup.infrastructure.ExamGroupRepository;
import com.platform.scheduling.examgroup.infrastructure.ExamRoomAllocationRepository;
import com.platform.scheduling.moduleexam.infrastructure.ModuleExamRepository;
import com.platform.scheduling.examschedule.presentation.dto.CreateExamSchedule;
import com.platform.scheduling.examschedule.presentation.dto.ExamScheduleResponse;
import com.platform.scheduling.examschedule.presentation.dto.UpdateExamScheduleRequest;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.universitygovernance.semester.domain.Semester;
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ExamScheduleService {

    private final EstablishmentRepository establishmentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;
    private final ModuleExamRepository moduleExamRepository;
    private final ExamGroupRepository examGroupRepository;
    private final ExamRoomAllocationRepository allocationRepository;

    public ExamScheduleService(
        EstablishmentRepository establishmentRepository,
        AcademicYearRepository academicYearRepository,
        SemesterRepository semesterRepository,
        ExamScheduleRepository examScheduleRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService,
        ModuleExamRepository moduleExamRepository,
        ExamGroupRepository examGroupRepository,
        ExamRoomAllocationRepository allocationRepository
    ) {
        this.establishmentRepository = establishmentRepository;
        this.academicYearRepository = academicYearRepository;
        this.semesterRepository = semesterRepository;
        this.examScheduleRepository = examScheduleRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
        this.moduleExamRepository = moduleExamRepository;
        this.examGroupRepository = examGroupRepository;
        this.allocationRepository = allocationRepository;
    }

    @Transactional
    public ExamScheduleResponse createExamSchedule(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        CreateExamSchedule request
    ) {
        requirePermission(
            principal,
            establishmentId,
            PermissionCode.EXAM_SCHEDULE_CREATE
        );

        Establishment establishment = findEstablishment(establishmentId);
        AcademicYear academicYear = findAcademicYear(request.academicYearId());
        Semester semester = findSemester(request.semesterId());
        ensureScheduleContext(establishmentId, academicYear, semester);

        if (examScheduleRepository
            .existsByEstablishmentIdAndAcademicYearIdAndSemesterIdAndSessionType(
                establishmentId,
                academicYear.getId(),
                semester.getId(),
                request.sessionType()
            )) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "An exam schedule already exists for this establishment, academic year, semester, and session type"
            );
        }

        ExamSchedule examSchedule = new ExamSchedule();
        examSchedule.setEstablishment(establishment);
        examSchedule.setAcademicYear(academicYear);
        examSchedule.setSemester(semester);
        examSchedule.setSessionType(request.sessionType());
        examSchedule.setPublicationStatus(PublicationStatus.DRAFT);
        validateDates(request.startDate(), request.endDate());
        examSchedule.setStartDate(request.startDate());
        examSchedule.setEndDate(request.endDate());
        return toResponse(examScheduleRepository.save(examSchedule));
    }

    @Transactional(readOnly = true)
    public List<ExamScheduleResponse> getExamSchedules(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        requirePermission(
            principal,
            establishmentId,
            PermissionCode.EXAM_SCHEDULE_VIEW
        );
        findEstablishment(establishmentId);

        return examScheduleRepository
            .findByEstablishmentIdOrderByCreatedAtDesc(establishmentId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ExamScheduleResponse getExamSchedule(
        AuthenticatedUserPrincipal principal,
        UUID examScheduleId
    ) {
        ExamSchedule examSchedule = findExamSchedule(examScheduleId);
        requirePermission(
            principal,
            examSchedule.getEstablishment().getId(),
            PermissionCode.EXAM_SCHEDULE_VIEW
        );
        return toResponse(examSchedule);
    }

    @Transactional
    public ExamScheduleResponse updateExamSchedule(
        AuthenticatedUserPrincipal principal,
        UUID examScheduleId,
        UpdateExamScheduleRequest request
    ) {
        ExamSchedule examSchedule = findExamSchedule(examScheduleId);
        UUID establishmentId = examSchedule.getEstablishment().getId();
        requirePermission(
            principal,
            establishmentId,
            PermissionCode.EXAM_SCHEDULE_UPDATE
        );
        ensureDraft(examSchedule);

        AcademicYear academicYear = findAcademicYear(request.academicYearId());
        Semester semester = findSemester(request.semesterId());
        ensureScheduleContext(establishmentId, academicYear, semester);

        if (examScheduleRepository
            .existsByEstablishmentIdAndAcademicYearIdAndSemesterIdAndSessionTypeAndIdNot(
                establishmentId,
                academicYear.getId(),
                semester.getId(),
                request.sessionType(),
                examScheduleId
            )) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "An exam schedule already exists for this establishment, academic year, semester, and session type"
            );
        }

        examSchedule.setAcademicYear(academicYear);
        examSchedule.setSemester(semester);
        examSchedule.setSessionType(request.sessionType());
        validateDates(request.startDate(), request.endDate());
        examSchedule.setStartDate(request.startDate());
        examSchedule.setEndDate(request.endDate());
        return toResponse(examScheduleRepository.save(examSchedule));
    }

    @Transactional
    public ActionResponse deleteExamSchedule(
        AuthenticatedUserPrincipal principal,
        UUID examScheduleId
    ) {
        ExamSchedule examSchedule = findExamSchedule(examScheduleId);
        requirePermission(
            principal,
            examSchedule.getEstablishment().getId(),
            PermissionCode.EXAM_SCHEDULE_DELETE
        );
        ensureDraft(examSchedule);
        examScheduleRepository.delete(examSchedule);
        return new ActionResponse(true, "Exam schedule deleted");
    }

    @Transactional
    public ExamScheduleResponse publishExamSchedule(
        AuthenticatedUserPrincipal principal,
        UUID examScheduleId
    ) {
        ExamSchedule examSchedule = findExamSchedule(examScheduleId);
        requirePermission(
            principal,
            examSchedule.getEstablishment().getId(),
            PermissionCode.EXAM_SCHEDULE_PUBLISH
        );

        if (examSchedule.getPublicationStatus() == PublicationStatus.PUBLISHED) {
            return toResponse(examSchedule);
        }

        if (!LocalDate.now().isAfter(examSchedule.getSemester().getEndDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The exam schedule cannot be published before the semester is finished");
        }

        moduleExamRepository.findByExamScheduleIdOrderByExamDateAscStartTimeAsc(examScheduleId).forEach(exam -> {
            long groupCount = examGroupRepository.findByExamScheduleIdAndClassGroupIdOrderByGroupOrderAsc(examScheduleId, exam.getClassGroup().getId()).size();
            if (groupCount > 0 && allocationRepository.countByModuleExamId(exam.getId()) != groupCount) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Every module exam must allocate one room to each exam group before publication");
            }
        });

        examSchedule.setPublicationStatus(PublicationStatus.PUBLISHED);
        return toResponse(examScheduleRepository.save(examSchedule));
    }

    private Establishment findEstablishment(UUID establishmentId) {
        return establishmentRepository.findById(establishmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Establishment not found"
            ));
    }

    private AcademicYear findAcademicYear(UUID academicYearId) {
        return academicYearRepository.findById(academicYearId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic year not found"
            ));
    }

    private Semester findSemester(UUID semesterId) {
        return semesterRepository.findById(semesterId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Semester not found"
            ));
    }

    private ExamSchedule findExamSchedule(UUID examScheduleId) {
        return examScheduleRepository.findById(examScheduleId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Exam schedule not found"
            ));
    }

    private void ensureDraft(ExamSchedule examSchedule) {
        if (examSchedule.getPublicationStatus() == PublicationStatus.PUBLISHED) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A published exam schedule cannot be changed"
            );
        }
    }

    private void ensureScheduleContext(
        UUID establishmentId,
        AcademicYear academicYear,
        Semester semester
    ) {
        UUID semesterEstablishmentId = semester
            .getAcademicLevel()
            .getProgramFiliere()
            .getDepartment()
            .getEstablishment()
            .getId();

        if (!establishmentId.equals(academicYear.getEstablishment().getId())
            || !establishmentId.equals(semesterEstablishmentId)
            || !academicYear.getId().equals(semester.getAcademicYear().getId())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Academic year and semester must belong to the establishment and the same academic period"
            );
        }
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exam period end date must be on or after its start date");
        }
    }

    private void requirePermission(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        PermissionCode permissionCode
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            permissionCode
        );
    }

    private ExamScheduleResponse toResponse(ExamSchedule schedule) {
        return new ExamScheduleResponse(
            schedule.getId(),
            schedule.getEstablishment().getId(),
            schedule.getAcademicYear().getId(),
            schedule.getSemester().getId(),
            schedule.getSessionType(),
            schedule.getPublicationStatus(),
            schedule.getStartDate(),
            schedule.getEndDate(),
            schedule.getCreatedAt(),
            schedule.getUpdatedAt()
        );
    }
}
