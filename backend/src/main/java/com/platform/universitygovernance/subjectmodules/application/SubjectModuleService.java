package com.platform.universitygovernance.subjectmodules.application;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.academicdomain.domain.AcademicDomain;
import com.platform.universitygovernance.academicdomain.infrastructure.AcademicDomainRepository;
import com.platform.universitygovernance.semester.domain.Semester;
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;
import com.platform.universitygovernance.subjectmodules.domain.SubjectModule;
import com.platform.universitygovernance.subjectmodules.domain.SubjectModuleDomain;
import com.platform.universitygovernance.subjectmodules.infrastructure.SubjectModuleDomainRepository;
import com.platform.universitygovernance.subjectmodules.infrastructure.SubjectModuleRepository;
import com.platform.universitygovernance.subjectmodules.presentation.dto.CreateSubjectModuleRequest;
import com.platform.universitygovernance.subjectmodules.presentation.dto.SubjectModuleResponse;
import com.platform.universitygovernance.subjectmodules.presentation.dto.UpdateSubjectModuleRequest;

import org.springframework.transaction.annotation.Transactional;

@Service
public class SubjectModuleService {

    private final SubjectModuleRepository subjectModuleRepository;
    private final SemesterRepository semesterRepository;
    private final AcademicDomainRepository academicDomainRepository;
    private final SubjectModuleDomainRepository subjectModuleDomainRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public SubjectModuleService(
        SubjectModuleRepository subjectModuleRepository,
        SemesterRepository semesterRepository,
        AcademicDomainRepository academicDomainRepository,
        SubjectModuleDomainRepository subjectModuleDomainRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.subjectModuleRepository = subjectModuleRepository;
        this.semesterRepository = semesterRepository;
        this.academicDomainRepository = academicDomainRepository;
        this.subjectModuleDomainRepository = subjectModuleDomainRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public SubjectModuleResponse createSubjectModule(
        AuthenticatedUserPrincipal principal,
        UUID semesterId,
        CreateSubjectModuleRequest request
    ) {
        Semester semester = findSemester(semesterId);
        requirePermission(principal, semester, PermissionCode.SUBJECT_MODULE_CREATE);
        String code = normalizeCode(request.code());
        String title = request.title().trim();
        ensureSubjectModuleAvailability(semesterId, code);

        SubjectModule subjectModule = new SubjectModule();
        subjectModule.setSemester(semester);
        subjectModule.setCode(code);
        subjectModule.setTitle(title);

        SubjectModule savedSubjectModule = subjectModuleRepository.save(subjectModule);
        replaceAcademicDomains(savedSubjectModule, request.academicDomainIds());
        return toResponse(savedSubjectModule);
    }

    @Transactional
    public SubjectModuleResponse updateSubjectModule(
        AuthenticatedUserPrincipal principal,
        UUID subjectModuleId,
        UpdateSubjectModuleRequest request
    ) {
        SubjectModule subjectModule = findSubjectModule(subjectModuleId);
        Semester semester = subjectModule.getSemester();
        requirePermission(principal, semester, PermissionCode.SUBJECT_MODULE_UPDATE);

        String code = normalizeCode(request.code());
        String title = request.title().trim();
        ensureSubjectModuleAvailability(semester.getId(), code, subjectModuleId);

        subjectModule.setCode(code);
        subjectModule.setTitle(title);

        SubjectModule savedSubjectModule = subjectModuleRepository.save(subjectModule);
        replaceAcademicDomains(savedSubjectModule, request.academicDomainIds());
        return toResponse(savedSubjectModule);
    }

    @Transactional(readOnly = true)
    public List<SubjectModuleResponse> getSubjectModules(
        AuthenticatedUserPrincipal principal,
        UUID semesterId
    ) {
        Semester semester = findSemester(semesterId);
        requirePermission(principal, semester, PermissionCode.SUBJECT_MODULE_VIEW);

        return subjectModuleRepository.findBySemesterIdOrderByCodeAsc(semesterId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public SubjectModuleResponse getSubjectModule(
        AuthenticatedUserPrincipal principal,
        UUID subjectModuleId
    ) {
        SubjectModule subjectModule = findSubjectModule(subjectModuleId);
        requirePermission(
            principal,
            subjectModule.getSemester(),
            PermissionCode.SUBJECT_MODULE_VIEW
        );
        return toResponse(subjectModule);
    }

    @Transactional
    public ActionResponse deleteSubjectModule(
        AuthenticatedUserPrincipal principal,
        UUID subjectModuleId
    ) {
        SubjectModule subjectModule = findSubjectModule(subjectModuleId);
        requirePermission(
            principal,
            subjectModule.getSemester(),
            PermissionCode.SUBJECT_MODULE_DELETE
        );
        subjectModuleDomainRepository.deleteAll(
            subjectModuleDomainRepository.findBySubjectModuleId(subjectModuleId)
        );
        subjectModuleRepository.delete(subjectModule);
        return new ActionResponse(true, "Subject module deleted");
    }

    public Semester findSemester(UUID semesterId) {
        return semesterRepository.findById(semesterId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Semester not found"
            ));
    }

    public SubjectModule findSubjectModule(UUID subjectModuleId) {
        return subjectModuleRepository.findById(subjectModuleId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Subject module not found"
            ));
    }

    public void requirePermission(
        AuthenticatedUserPrincipal principal,
        Semester semester,
        PermissionCode permissionCode
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            semester.getAcademicLevel().getProgramFiliere().getDepartment().getEstablishment().getId(),
            permissionCode
        );
    }

    public void ensureSubjectModuleAvailability(
        UUID semesterId,
        String code
    ) {
        if (subjectModuleRepository.existsBySemesterIdAndCodeIgnoreCase(semesterId, code)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A subject module with this code already exists in the semester"
            );
        }
    }

    public void ensureSubjectModuleAvailability(
        UUID semesterId,
        String code,
        UUID subjectModuleId
    ) {
        if (subjectModuleRepository.existsBySemesterIdAndCodeIgnoreCaseAndIdNot(
            semesterId,
            code,
            subjectModuleId
        )) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A subject module with this code already exists in the semester"
            );
        }
    }

    private SubjectModuleResponse toResponse(SubjectModule subjectModule) {
        List<UUID> academicDomainIds = subjectModuleDomainRepository
            .findBySubjectModuleId(subjectModule.getId())
            .stream()
            .map(link -> link.getAcademicDomain().getId())
            .sorted()
            .toList();
        return new SubjectModuleResponse(
            subjectModule.getId(),
            subjectModule.getSemester().getId(),
            subjectModule.getCode(),
            subjectModule.getTitle(),
            academicDomainIds
        );
    }

    private void replaceAcademicDomains(
        SubjectModule subjectModule,
        Set<UUID> academicDomainIds
    ) {
        List<AcademicDomain> academicDomains = academicDomainRepository
            .findAllById(academicDomainIds);
        if (academicDomains.size() != academicDomainIds.size()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Academic domain not found"
            );
        }

        UUID establishmentId = subjectModule.getSemester()
            .getAcademicLevel()
            .getProgramFiliere()
            .getDepartment()
            .getEstablishment()
            .getId();
        if (academicDomains.stream().anyMatch(domain ->
            !establishmentId.equals(domain.getEstablishment().getId()))) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Academic domains must belong to the subject module's establishment"
            );
        }

        subjectModuleDomainRepository.deleteAll(
            subjectModuleDomainRepository.findBySubjectModuleId(subjectModule.getId())
        );
        subjectModuleDomainRepository.flush();
        List<SubjectModuleDomain> links = academicDomains.stream()
            .map(domain -> {
                SubjectModuleDomain link = new SubjectModuleDomain();
                link.setSubjectModule(subjectModule);
                link.setAcademicDomain(domain);
                return link;
            })
            .toList();
        subjectModuleDomainRepository.saveAll(links);
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
