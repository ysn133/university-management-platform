package com.platform.universitygovernance.semester.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academiclevel.infrastructure.AcademicLevelRepository;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.semester.domain.Semester;
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;
import com.platform.universitygovernance.semester.presentation.dto.CreateSemesterRequest;
import com.platform.universitygovernance.semester.presentation.dto.SemesterResponse;
import com.platform.universitygovernance.semester.presentation.dto.UpdateSemesterRequest;
import java.util.List;
import java.time.LocalDate;
import com.platform.universitygovernance.semester.domain.SemesterLifecycleStatus;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SemesterService {

    private final SemesterRepository semesterRepository;
    private final AcademicLevelRepository academicLevelRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public SemesterService(
        SemesterRepository semesterRepository,
        AcademicLevelRepository academicLevelRepository,
        AcademicYearRepository academicYearRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.semesterRepository = semesterRepository;
        this.academicLevelRepository = academicLevelRepository;
        this.academicYearRepository = academicYearRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public SemesterResponse createSemester(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId,
        UUID academicYearId,
        CreateSemesterRequest request
    ) {
        AcademicLevel academicLevel = findAcademicLevel(academicLevelId);
        AcademicYear academicYear = findAcademicYear(academicYearId);
        UUID establishmentId = establishmentId(academicLevel);
        ensureSameEstablishment(establishmentId, academicYear);
        requirePermission(principal, establishmentId, PermissionCode.SEMESTER_CREATE);

        String name = normalizeName(request.name());
        ensureSemesterAvailable(
            academicLevelId,
            academicYearId,
            name,
            request.semesterOrder(),
            null
        );

        Semester semester = new Semester();
        semester.setAcademicLevel(academicLevel);
        semester.setAcademicYear(academicYear);
        semester.setName(name);
        semester.setSemesterOrder(request.semesterOrder());
        semester.setTermType(request.termType());
        validateDates(request.startDate(), request.endDate());
        semester.setStartDate(request.startDate());
        semester.setEndDate(request.endDate());
        return toResponse(semesterRepository.save(semester));
    }

    @Transactional(readOnly = true)
    public List<SemesterResponse> getSemesters(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId,
        UUID academicYearId
    ) {
        AcademicLevel academicLevel = findAcademicLevel(academicLevelId);
        AcademicYear academicYear = findAcademicYear(academicYearId);
        UUID establishmentId = establishmentId(academicLevel);
        ensureSameEstablishment(establishmentId, academicYear);
        requirePermission(principal, establishmentId, PermissionCode.SEMESTER_VIEW);

        return semesterRepository
            .findByAcademicLevelIdAndAcademicYearIdOrderBySemesterOrderAsc(
                academicLevelId,
                academicYearId
            )
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public SemesterResponse getSemester(
        AuthenticatedUserPrincipal principal,
        UUID semesterId
    ) {
        Semester semester = findSemester(semesterId);
        requirePermission(
            principal,
            establishmentId(semester.getAcademicLevel()),
            PermissionCode.SEMESTER_VIEW
        );
        return toResponse(semester);
    }

    @Transactional
    public SemesterResponse updateSemester(
        AuthenticatedUserPrincipal principal,
        UUID semesterId,
        UpdateSemesterRequest request
    ) {
        Semester semester = findSemester(semesterId);
        UUID establishmentId = establishmentId(semester.getAcademicLevel());
        requirePermission(principal, establishmentId, PermissionCode.SEMESTER_UPDATE);

        String name = normalizeName(request.name());
        ensureSemesterAvailable(
            semester.getAcademicLevel().getId(),
            semester.getAcademicYear().getId(),
            name,
            request.semesterOrder(),
            semesterId
        );
        semester.setName(name);
        semester.setSemesterOrder(request.semesterOrder());
        semester.setTermType(request.termType());
        validateDates(request.startDate(), request.endDate());
        semester.setStartDate(request.startDate());
        semester.setEndDate(request.endDate());
        return toResponse(semesterRepository.save(semester));
    }

    @Transactional
    public ActionResponse deleteSemester(
        AuthenticatedUserPrincipal principal,
        UUID semesterId
    ) {
        Semester semester = findSemester(semesterId);
        requirePermission(
            principal,
            establishmentId(semester.getAcademicLevel()),
            PermissionCode.SEMESTER_DELETE
        );
        semesterRepository.delete(semester);
        return new ActionResponse(true, "Semester deleted");
    }

    private AcademicLevel findAcademicLevel(UUID academicLevelId) {
        return academicLevelRepository.findById(academicLevelId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic level not found"
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

    private UUID establishmentId(AcademicLevel academicLevel) {
        return academicLevel.getProgramFiliere().getDepartment().getEstablishment().getId();
    }

    private void ensureSameEstablishment(UUID establishmentId, AcademicYear academicYear) {
        if (!establishmentId.equals(academicYear.getEstablishment().getId())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Academic level and academic year must belong to the same establishment"
            );
        }
    }

    private void requirePermission(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        PermissionCode permissionCode
    ) {
        permissionAuthorizationService.requirePermission(principal, establishmentId, permissionCode);
    }

    private void ensureSemesterAvailable(
        UUID academicLevelId,
        UUID academicYearId,
        String name,
        int semesterOrder,
        UUID semesterId
    ) {
        boolean nameExists = semesterId == null
            ? semesterRepository.existsByAcademicLevelIdAndAcademicYearIdAndNameIgnoreCase(
                academicLevelId,
                academicYearId,
                name
            )
            : semesterRepository.existsByAcademicLevelIdAndAcademicYearIdAndNameIgnoreCaseAndIdNot(
                academicLevelId,
                academicYearId,
                name,
                semesterId
            );
        if (nameExists) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A semester with this name already exists for the academic level and year"
            );
        }

        boolean orderExists = semesterId == null
            ? semesterRepository.existsByAcademicLevelIdAndAcademicYearIdAndSemesterOrder(
                academicLevelId,
                academicYearId,
                semesterOrder
            )
            : semesterRepository.existsByAcademicLevelIdAndAcademicYearIdAndSemesterOrderAndIdNot(
                academicLevelId,
                academicYearId,
                semesterOrder,
                semesterId
            );
        if (orderExists) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A semester with this order already exists for the academic level and year"
            );
        }
    }

    private String normalizeName(String name) {
        return name.trim().toUpperCase();
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Semester end date must be on or after its start date");
        }
    }

    private SemesterLifecycleStatus lifecycleStatus(Semester semester) {
        LocalDate today = LocalDate.now();
        if (today.isBefore(semester.getStartDate())) return SemesterLifecycleStatus.PLANNED;
        if (today.isAfter(semester.getEndDate())) return SemesterLifecycleStatus.FINISHED;
        return SemesterLifecycleStatus.ACTIVE;
    }

    private SemesterResponse toResponse(Semester semester) {
        return new SemesterResponse(
            semester.getId(),
            semester.getAcademicLevel().getId(),
            semester.getAcademicYear().getId(),
            establishmentId(semester.getAcademicLevel()),
            semester.getName(),
            semester.getSemesterOrder(),
            semester.getTermType(),
            semester.getStartDate(),
            semester.getEndDate(),
            lifecycleStatus(semester),
            semester.getCreatedAt(),
            semester.getUpdatedAt()
        );
    }
}
