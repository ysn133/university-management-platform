package com.platform.universitygovernance.establishment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import com.platform.universitygovernance.establishment.domain.Establishment;

import java.util.List;
import java.util.UUID;


public interface EstablishmentRepository extends JpaRepository<Establishment, UUID> {

  
    List<Establishment> findByUniversityId(UUID universityId);
    
    
    
}
