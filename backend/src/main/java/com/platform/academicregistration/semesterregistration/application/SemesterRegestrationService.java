package com.platform.academicregistration.semesterregistration.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.platform.academicregistration.registration.domain.AcademicRegistration;
import com.platform.academicregistration.semesterregistration.domain.SemesterRegestration;
import com.platform.academicregistration.semesterregistration.infrastructer.SemesterRegestrationRepository;
import com.platform.academicregistration.subjectmoduleregestration.application.SubjectModuleRegestrationService;
import com.platform.universitygovernance.semester.domain.Semester;
import com.platform.universitygovernance.semester.infrastructure.SemesterRepository;


@Service
public class SemesterRegestrationService {

    private final SemesterRepository semesterRepository;
    private final SemesterRegestrationRepository semesterRegestrationRepository;
    private final SubjectModuleRegestrationService subjectModuleRegestration;


    public SemesterRegestrationService(SemesterRepository semesterRepository , 
        SemesterRegestrationRepository semesterRegestrationRepository,
        SubjectModuleRegestrationService subjectModuleRegestration
    ){
        this.semesterRegestrationRepository=semesterRegestrationRepository;
        this.semesterRepository=semesterRepository;
        this.subjectModuleRegestration=subjectModuleRegestration;
    }

    public List<SemesterRegestration> createSemesterRegestration(AcademicRegistration academicRegistration){

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

        List <SemesterRegestration> semesterRegistrations=new ArrayList<>();
        for(Semester semester:semesters){
            
            SemesterRegestration semesterRegestration  = new SemesterRegestration();
            semesterRegestration.setAcademicRegistration(academicRegistration);
            semesterRegestration.setSemester(semester);
            SemesterRegestration savedSemesterRegestration=semesterRegestrationRepository.save(semesterRegestration);
            subjectModuleRegestration.createModuleRegestration(
                semester,
                savedSemesterRegestration
            );

            semesterRegistrations.add(savedSemesterRegestration);


        }



        return semesterRegistrations;
    }





    
}
