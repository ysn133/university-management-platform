package com.platform.academicregistration.subjectmoduleregestration.infrastructure;
import com.platform.academicregistration.subjectmoduleregestration.domain.SubjectModuleRegestration;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRegestrationRepository extends JpaRepository<SubjectModuleRegestration ,UUID>{
    
}
