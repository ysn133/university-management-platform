package com.platform.academicregistration.subjectmoduleregestration.application;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.platform.academicregistration.semesterregistration.domain.SemesterRegestration;
import com.platform.academicregistration.subjectmoduleregestration.domain.SubjectModuleRegestration;
import com.platform.academicregistration.subjectmoduleregestration.domain.SubjectModuleRegistrationStatus;
import com.platform.academicregistration.subjectmoduleregestration.infrastructure.SubjectRegestrationRepository;
import com.platform.universitygovernance.semester.domain.Semester;
import com.platform.universitygovernance.subjectmodules.domain.SubjectModule;
import com.platform.universitygovernance.subjectmodules.infrastructure.SubjectModuleRepository;

@Service
public class SubjectModuleRegestrationService {


    private final SubjectRegestrationRepository subjectRegestrationRepository;
    private final SubjectModuleRepository subjectModuleRepository;

    public SubjectModuleRegestrationService(
        SubjectRegestrationRepository subjectRegestrationRepository,
        SubjectModuleRepository subjectModuleRepository
    ){
        this.subjectModuleRepository=subjectModuleRepository;
        this.subjectRegestrationRepository=subjectRegestrationRepository;

    }

    public List<SubjectModuleRegestration> createModuleRegestration(
        Semester semester,
        SemesterRegestration semesterRegistration
    ) {
    

       

       
             List <SubjectModule> subjectModules=subjectModuleRepository.findBySemesterIdOrderByCodeAsc(semester.getId());

             List<SubjectModuleRegestration> regestrations=new ArrayList<>();

             for(SubjectModule subjectModule:subjectModules){
                SubjectModuleRegestration subjectModuleRegestration=new SubjectModuleRegestration();
                subjectModuleRegestration.setSemesterRegestration(semesterRegistration);
                subjectModuleRegestration.setSubjectModule(subjectModule);
                subjectModuleRegestration.setInscriptionNumber(1);
                subjectModuleRegestration.setStatus(SubjectModuleRegistrationStatus.ACTIVE);
                regestrations.add(subjectRegestrationRepository.save(subjectModuleRegestration));

             }
            

        


        return regestrations;
        
    }


    
}
