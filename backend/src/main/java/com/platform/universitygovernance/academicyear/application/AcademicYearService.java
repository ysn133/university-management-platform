package com.platform.universitygovernance.academicyear.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.academicyear.presentation.dto.AcademicYearResponse;
import com.platform.universitygovernance.academicyear.presentation.dto.CreateAcademicYearRequest;
import com.platform.universitygovernance.academicyear.presentation.dto.UpdateAcademicYearRequest;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final EstablishmentRepository establishmentRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public AcademicYearService(
        AcademicYearRepository academicYearRepository,
        EstablishmentRepository establishmentRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.academicYearRepository = academicYearRepository;
        this.establishmentRepository = establishmentRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public AcademicYearResponse createAcademicYear(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        CreateAcademicYearRequest request
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.ACADEMIC_YEAR_CREATE
        );
        Establishment establishment = findEstablishment(establishmentId);
        AcademicYearPeriod period = parsePeriod(request.label());
        ensureLabelAvailable(establishmentId, period.label(), null);

        AcademicYear academicYear = new AcademicYear();
        academicYear.setEstablishment(establishment);
        applyPeriod(academicYear, period);
        academicYear.setStatus(request.status());
        return toResponse(academicYearRepository.save(academicYear));
    }

    @Transactional(readOnly = true)
    public List<AcademicYearResponse> getAcademicYears(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.ACADEMIC_YEAR_VIEW
        );
        findEstablishment(establishmentId);
        return academicYearRepository.findByEstablishmentIdOrderByStartYearDesc(establishmentId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public AcademicYearResponse getAcademicYear(
        AuthenticatedUserPrincipal principal,
        UUID academicYearId
    ) {
        AcademicYear academicYear = findAcademicYear(academicYearId);
        permissionAuthorizationService.requirePermission(
            principal,
            academicYear.getEstablishment().getId(),
            PermissionCode.ACADEMIC_YEAR_VIEW
        );
        return toResponse(academicYear);
    }

    @Transactional
    public AcademicYearResponse updateAcademicYear(
        AuthenticatedUserPrincipal principal,
        UUID academicYearId,
        UpdateAcademicYearRequest request
    ) {
        AcademicYear academicYear = findAcademicYear(academicYearId);
        UUID establishmentId = academicYear.getEstablishment().getId();
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            PermissionCode.ACADEMIC_YEAR_UPDATE
        );

        AcademicYearPeriod period = parsePeriod(request.label());
        ensureLabelAvailable(establishmentId, period.label(), academicYearId);
        applyPeriod(academicYear, period);
        academicYear.setStatus(request.status());
        return toResponse(academicYearRepository.save(academicYear));
    }

    @Transactional
    public ActionResponse deleteAcademicYear(
        AuthenticatedUserPrincipal principal,
        UUID academicYearId
    ) {
        AcademicYear academicYear = findAcademicYear(academicYearId);
        permissionAuthorizationService.requirePermission(
            principal,
            academicYear.getEstablishment().getId(),
            PermissionCode.ACADEMIC_YEAR_DELETE
        );
        academicYearRepository.delete(academicYear);
        return new ActionResponse(true, "Academic year deleted");
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

    private void ensureLabelAvailable(
        UUID establishmentId,
        String label,
        UUID academicYearId
    ) {
        boolean exists = academicYearId == null
            ? academicYearRepository.existsByEstablishmentIdAndLabel(establishmentId, label)
            : academicYearRepository.existsByEstablishmentIdAndLabelAndIdNot(
                establishmentId,
                label,
                academicYearId
            );
        if (exists) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "An academic year with this label already exists in the establishment"
            );
        }
    }

    private AcademicYearPeriod parsePeriod(String value) {
        String label = value == null ? "" : value.trim();
        if (!label.matches("\\d{4}-\\d{4}")) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Academic year label must use the format YYYY-YYYY"
            );
        }

        int startYear = Integer.parseInt(label.substring(0, 4));
        int endYear = Integer.parseInt(label.substring(5, 9));
        if (endYear != startYear + 1) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Academic year label must contain two consecutive years"
            );
        }
        return new AcademicYearPeriod(label, startYear, endYear);
    }

    private void applyPeriod(AcademicYear academicYear, AcademicYearPeriod period) {
        academicYear.setLabel(period.label());
        academicYear.setStartYear(period.startYear());
        academicYear.setEndYear(period.endYear());
    }

    private AcademicYearResponse toResponse(AcademicYear academicYear) {
        return new AcademicYearResponse(
            academicYear.getId(),
            academicYear.getEstablishment().getId(),
            academicYear.getLabel(),
            academicYear.getStartYear(),
            academicYear.getEndYear(),
            academicYear.getStatus(),
            academicYear.getCreatedAt(),
            academicYear.getUpdatedAt()
        );
    }

    private record AcademicYearPeriod(String label, int startYear, int endYear) {
    }
}
