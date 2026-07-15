package com.platform.universitygovernance.classgroup.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.academiclevel.domain.AcademicLevel;
import com.platform.universitygovernance.academiclevel.infrastructure.AcademicLevelRepository;
import com.platform.universitygovernance.academicyear.domain.AcademicYear;
import com.platform.universitygovernance.academicyear.infrastructure.AcademicYearRepository;
import com.platform.universitygovernance.classgroup.domain.ClassGroup;
import com.platform.universitygovernance.classgroup.infrastructure.ClassGroupRepository;
import com.platform.universitygovernance.classgroup.presentation.dto.ClassGroupResponse;
import com.platform.universitygovernance.classgroup.presentation.dto.CreateClassGroupRequest;
import com.platform.universitygovernance.classgroup.presentation.dto.UpdateClassGroupRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClassGroupService {

    private final ClassGroupRepository classGroupRepository;
    private final AcademicLevelRepository academicLevelRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public ClassGroupService(
        ClassGroupRepository classGroupRepository,
        AcademicLevelRepository academicLevelRepository,
        AcademicYearRepository academicYearRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.classGroupRepository = classGroupRepository;
        this.academicLevelRepository = academicLevelRepository;
        this.academicYearRepository = academicYearRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public ClassGroupResponse createClassGroup(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId,
        UUID academicYearId,
        CreateClassGroupRequest request
    ) {
        AcademicLevel academicLevel = findAcademicLevel(academicLevelId);
        AcademicYear academicYear = findAcademicYear(academicYearId);
        UUID establishmentId = establishmentId(academicLevel);
        ensureSameEstablishment(establishmentId, academicYear);
        requirePermission(principal, establishmentId, PermissionCode.CLASS_GROUP_CREATE);

        String name = normalizeName(request.name());
        ensureNameAvailable(academicLevelId, academicYearId, name, null);

        ClassGroup classGroup = new ClassGroup();
        classGroup.setAcademicLevel(academicLevel);
        classGroup.setAcademicYear(academicYear);
        classGroup.setName(name);
        classGroup.setStatus(request.status());
        return toResponse(classGroupRepository.save(classGroup));
    }

    @Transactional(readOnly = true)
    public List<ClassGroupResponse> getClassGroups(
        AuthenticatedUserPrincipal principal,
        UUID academicLevelId,
        UUID academicYearId
    ) {
        AcademicLevel academicLevel = findAcademicLevel(academicLevelId);
        AcademicYear academicYear = findAcademicYear(academicYearId);
        UUID establishmentId = establishmentId(academicLevel);
        ensureSameEstablishment(establishmentId, academicYear);
        requirePermission(principal, establishmentId, PermissionCode.CLASS_GROUP_VIEW);

        return classGroupRepository
            .findByAcademicLevelIdAndAcademicYearIdOrderByNameAsc(academicLevelId, academicYearId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ClassGroupResponse getClassGroup(
        AuthenticatedUserPrincipal principal,
        UUID classGroupId
    ) {
        ClassGroup classGroup = findClassGroup(classGroupId);
        requirePermission(
            principal,
            establishmentId(classGroup.getAcademicLevel()),
            PermissionCode.CLASS_GROUP_VIEW
        );
        return toResponse(classGroup);
    }

    @Transactional
    public ClassGroupResponse updateClassGroup(
        AuthenticatedUserPrincipal principal,
        UUID classGroupId,
        UpdateClassGroupRequest request
    ) {
        ClassGroup classGroup = findClassGroup(classGroupId);
        UUID establishmentId = establishmentId(classGroup.getAcademicLevel());
        requirePermission(principal, establishmentId, PermissionCode.CLASS_GROUP_UPDATE);

        String name = normalizeName(request.name());
        ensureNameAvailable(
            classGroup.getAcademicLevel().getId(),
            classGroup.getAcademicYear().getId(),
            name,
            classGroupId
        );
        classGroup.setName(name);
        classGroup.setStatus(request.status());
        return toResponse(classGroupRepository.save(classGroup));
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

    private ClassGroup findClassGroup(UUID classGroupId) {
        return classGroupRepository.findById(classGroupId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Class group not found"
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

    private void ensureNameAvailable(
        UUID academicLevelId,
        UUID academicYearId,
        String name,
        UUID classGroupId
    ) {
        boolean exists = classGroupId == null
            ? classGroupRepository.existsByAcademicLevelIdAndAcademicYearIdAndNameIgnoreCase(
                academicLevelId,
                academicYearId,
                name
            )
            : classGroupRepository.existsByAcademicLevelIdAndAcademicYearIdAndNameIgnoreCaseAndIdNot(
                academicLevelId,
                academicYearId,
                name,
                classGroupId
            );
        if (exists) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A class group with this name already exists for the academic level and year"
            );
        }
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private ClassGroupResponse toResponse(ClassGroup classGroup) {
        AcademicLevel academicLevel = classGroup.getAcademicLevel();
        return new ClassGroupResponse(
            classGroup.getId(),
            academicLevel.getId(),
            classGroup.getAcademicYear().getId(),
            academicLevel.getProgramFiliere().getId(),
            establishmentId(academicLevel),
            classGroup.getName(),
            classGroup.getStatus(),
            classGroup.getCreatedAt(),
            classGroup.getUpdatedAt()
        );
    }
}
