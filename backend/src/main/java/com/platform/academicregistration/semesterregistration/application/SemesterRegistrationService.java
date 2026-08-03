package com.platform.academicregistration.semesterregistration.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.platform.academicregistration.registration.domain.AcademicRegistration;
import com.platform.academicregistration.semesterregistration.domain.SemesterRegistration;
import com.platform.academicregistration.semesterregistration.infrastructure.SemesterRegistrationRepository;
import com.platform.academicregistration.moduleregistration.application.ModuleRegistrationService;
import com.platform.academicregistration.semesterregistration.presentation.dto.SemesterRegistrationResponse;
import com.platform.universitygovernance.semester.domain.Semester;
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;


@Service
public class SemesterRegistrationService {

    private final SemesterRepository semesterRepository;
    private final SemesterRegistrationRepository semesterRegistrationRepository;
    private final ModuleRegistrationService moduleRegistration;


    public SemesterRegistrationService(SemesterRepository semesterRepository,
        SemesterRegistrationRepository semesterRegistrationRepository,
        ModuleRegistrationService moduleRegistration
    ){
        this.semesterRegistrationRepository=semesterRegistrationRepository;
        this.semesterRepository=semesterRepository;
        this.moduleRegistration=moduleRegistration;
    }

    public List<SemesterRegistration> createSemesterRegistration(AcademicRegistration academicRegistration){

        UUID academicLevelId=academicRegistration.getAcademicLevel().getId();
        UUID academicYearId  = academicRegistration.getAcademicYear().getId();

        List <Semester> semesters=semesterRepository.findByAcademicLevelIdAndAcademicYearIdOrderBySemesterOrderAsc(
            academicLevelId,
            academicYearId
        );

        if (semesters.size() != 2) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Exactly two semesters must be configured for the academic level and academic year"
            );
        }

        List <SemesterRegistration> semesterRegistrations=new ArrayList<>();
        for(Semester semester:semesters){
            
            SemesterRegistration semesterRegistration  = new SemesterRegistration();
            semesterRegistration.setAcademicRegistration(academicRegistration);
            semesterRegistration.setSemester(semester);
            SemesterRegistration savedSemesterRegistration=semesterRegistrationRepository.save(semesterRegistration);
            moduleRegistration.createModuleRegistration(
                semester,
                savedSemesterRegistration
            );

            semesterRegistrations.add(savedSemesterRegistration);


        }



        return semesterRegistrations;
    }

    public List<SemesterRegistrationResponse> getByAcademicRegistration(
        UUID academicRegistrationId
    ) {
        return semesterRegistrationRepository
            .findByAcademicRegistrationId(academicRegistrationId)
            .stream()
            .sorted(java.util.Comparator.comparingInt(
                registration -> registration.getSemester().getSemesterOrder()
            ))
            .map(this::toResponse)
            .toList();
    }

    private SemesterRegistrationResponse toResponse(SemesterRegistration registration) {
        return new SemesterRegistrationResponse(
            registration.getId(),
            registration.getAcademicRegistration().getId(),
            registration.getSemester().getId(),
            registration.getSemester().getName(),
            registration.getSemester().getSemesterOrder()
        );
    }





    
}
