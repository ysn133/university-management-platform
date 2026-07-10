package com.platform.identityaccess.infrastructure;

import com.platform.identityaccess.domain.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByUniversityEmail(String universityEmail);

    boolean existsByUniversityEmail(String universityEmail);
}