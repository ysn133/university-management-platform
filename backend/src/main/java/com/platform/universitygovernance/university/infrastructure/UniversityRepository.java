package com.platform.universitygovernance.university.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.platform.universitygovernance.university.domain.University;

public interface UniversityRepository extends JpaRepository<University, UUID> {

    Optional<University> findFirstByOrderByCreatedAtAsc();
}
