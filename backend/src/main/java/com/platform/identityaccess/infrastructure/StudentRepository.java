package com.platform.identityaccess.infrastructure;

import com.platform.identityaccess.domain.Student;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findByUserAccountId(UUID userAccountId);

    boolean existsByApogeeCodeIgnoreCase(String apogeeCode);

    boolean existsByNationalStudentCodeIgnoreCase(String nationalStudentCode);

    List<Student> findByEstablishmentIdOrderByCreatedAtAsc(UUID establishmentId);
}
