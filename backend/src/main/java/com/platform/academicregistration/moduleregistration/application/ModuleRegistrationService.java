package com.platform.academicregistration.moduleregistration.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.platform.academicregistration.semesterregistration.domain.SemesterRegistration;
import com.platform.academicregistration.moduleregistration.domain.ModuleRegistration;
import com.platform.academicregistration.moduleregistration.domain.ModuleRegistrationStatus;
import com.platform.academicregistration.moduleregistration.infrastructure.ModuleRegistrationRepository;
import com.platform.academicregistration.moduleregistration.presentation.dto.ModuleRegistrationResponse;
import com.platform.universitygovernance.semester.domain.Semester;
import com.platform.universitygovernance.subjectmodules.domain.SubjectModule;
import com.platform.universitygovernance.subjectmodules.infrastructure.SubjectModuleRepository;

@Service
public class ModuleRegistrationService {


    private final ModuleRegistrationRepository moduleRegistrationRepository;
    private final SubjectModuleRepository subjectModuleRepository;

    public ModuleRegistrationService(
        ModuleRegistrationRepository moduleRegistrationRepository,
        SubjectModuleRepository subjectModuleRepository
    ){
        this.subjectModuleRepository=subjectModuleRepository;
        this.moduleRegistrationRepository=moduleRegistrationRepository;

    }

    public List<ModuleRegistration> createModuleRegistration(
        Semester semester,
        SemesterRegistration semesterRegistration
    ) {
        List<SubjectModule> subjectModules = subjectModuleRepository
            .findBySemesterIdOrderByCodeAsc(semester.getId());
        List<ModuleRegistration> registrations = new ArrayList<>();

        for (SubjectModule subjectModule : subjectModules) {
            ModuleRegistration moduleRegistration = new ModuleRegistration();
            moduleRegistration.setSemesterRegistration(semesterRegistration);
            moduleRegistration.setSubjectModule(subjectModule);
            moduleRegistration.setInscriptionNumber(1);
            moduleRegistration.setStatus(ModuleRegistrationStatus.ACTIVE);
            registrations.add(moduleRegistrationRepository.save(moduleRegistration));
        }

        return registrations;
    }

    public List<ModuleRegistrationResponse> getBySemesterRegistration(
        UUID semesterRegistrationId
    ) {
        return moduleRegistrationRepository
            .findBySemesterRegistrationIdOrderBySubjectModuleCodeAsc(semesterRegistrationId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private ModuleRegistrationResponse toResponse(ModuleRegistration registration) {
        return new ModuleRegistrationResponse(
            registration.getId(),
            registration.getSemesterRegistration().getId(),
            registration.getSubjectModule().getId(),
            registration.getSubjectModule().getCode(),
            registration.getSubjectModule().getTitle(),
            registration.getOriginAcademicLevel() == null
                ? null
                : registration.getOriginAcademicLevel().getId(),
            registration.getInscriptionNumber(),
            registration.getStatus()
        );
    }
}
