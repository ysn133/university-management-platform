package com.platform.identityaccess.infrastructure;

import com.platform.identityaccess.domain.Permission;
import com.platform.identityaccess.domain.PermissionCode;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    List<Permission> findAllByOrderByCodeAsc();

    List<Permission> findByCodeIn(Collection<PermissionCode> codes);
}
