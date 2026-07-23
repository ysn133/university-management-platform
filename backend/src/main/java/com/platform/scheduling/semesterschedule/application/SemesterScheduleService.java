package com.platform.scheduling.semesterschedule.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.semesterschedule.domain.SchedulePublicationStatus;
import com.platform.scheduling.semesterschedule.domain.SemesterSchedule;
import com.platform.scheduling.semesterschedule.infrastructure.SemesterScheduleRepository;
import com.platform.scheduling.semesterschedule.presentation.dto.CreateSemesterScheduleRequest;
import com.platform.scheduling.semesterschedule.presentation.dto.SemesterScheduleResponse;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.universitygovernance.semester.domain.Semester;
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SemesterScheduleService {

    private final SemesterScheduleRepository semesterScheduleRepository;
    private final EstablishmentRepository establishmentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public SemesterScheduleService(
        SemesterScheduleRepository semesterScheduleRepository,
        EstablishmentRepository establishmentRepository,
        AcademicYearRepository academicYearRepository,
        SemesterRepository semesterRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.semesterScheduleRepository = semesterScheduleRepository;
        this.establishmentRepository = establishmentRepository;
        this.academicYearRepository = academicYearRepository;
        this.semesterRepository = semesterRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public SemesterScheduleResponse createSemesterSchedule(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        CreateSemesterScheduleRequest request
    ) {
        requirePermission(
            principal,
            establishmentId,
            PermissionCode.SEMESTER_SCHEDULE_CREATE
        );

        Establishment establishment = findEstablishment(establishmentId);
        AcademicYear academicYear = findAcademicYear(request.academicYearId());
        Semester semester = findSemester(request.semesterId());
        ensureScheduleContext(establishmentId, academicYear, semester);

        if (semesterScheduleRepository
            .existsByEstablishmentIdAndAcademicYearIdAndSemesterId(
                establishmentId,
                academicYear.getId(),
                semester.getId()
            )) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A schedule already exists for this establishment, academic year, and semester"
            );
        }

        SemesterSchedule schedule = new SemesterSchedule();
        schedule.setEstablishment(establishment);
        schedule.setAcademicYear(academicYear);
        schedule.setSemester(semester);
        schedule.setPublicationStatus(SchedulePublicationStatus.DRAFT);
        return toResponse(semesterScheduleRepository.save(schedule));
    }

    @Transactional(readOnly = true)
    public List<SemesterScheduleResponse> getSemesterSchedules(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        requirePermission(
            principal,
            establishmentId,
            PermissionCode.SEMESTER_SCHEDULE_VIEW
        );
        findEstablishment(establishmentId);

        return semesterScheduleRepository
            .findByEstablishmentIdOrderByCreatedAtDesc(establishmentId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public SemesterScheduleResponse getSemesterSchedule(
        AuthenticatedUserPrincipal principal,
        UUID scheduleId
    ) {
        SemesterSchedule schedule = findSemesterSchedule(scheduleId);
        requirePermission(
            principal,
            schedule.getEstablishment().getId(),
            PermissionCode.SEMESTER_SCHEDULE_VIEW
        );
        return toResponse(schedule);
    }

    @Transactional
    public SemesterScheduleResponse publishSemesterSchedule(
        AuthenticatedUserPrincipal principal,
        UUID scheduleId
    ) {
        SemesterSchedule schedule = findSemesterSchedule(scheduleId);
        requirePermission(
            principal,
            schedule.getEstablishment().getId(),
            PermissionCode.SEMESTER_SCHEDULE_PUBLISH
        );

        if (schedule.getPublicationStatus() == SchedulePublicationStatus.PUBLISHED) {
            return toResponse(schedule);
        }

        schedule.setPublicationStatus(SchedulePublicationStatus.PUBLISHED);
        schedule.setPublishedAt(Instant.now());
        return toResponse(semesterScheduleRepository.save(schedule));
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

    private SemesterSchedule findSemesterSchedule(UUID scheduleId) {
        return semesterScheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Semester schedule not found"
            ));
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

    private SemesterScheduleResponse toResponse(SemesterSchedule schedule) {
        return new SemesterScheduleResponse(
            schedule.getId(),
            schedule.getEstablishment().getId(),
            schedule.getAcademicYear().getId(),
            schedule.getSemester().getId(),
            schedule.getPublicationStatus(),
            schedule.getPublishedAt(),
            schedule.getCreatedAt(),
            schedule.getUpdatedAt()
        );
    }
}
