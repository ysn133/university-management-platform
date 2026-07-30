package com.platform.usermanagement.professor.expertise.infrastructure;

import com.platform.usermanagement.professor.expertise.domain.ProfessorExpertise;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorExpertiseRepository extends JpaRepository<ProfessorExpertise, UUID> {

    List<ProfessorExpertise> findByProfessorIdOrderByAcademicDomainNameAsc(UUID professorId);
}
