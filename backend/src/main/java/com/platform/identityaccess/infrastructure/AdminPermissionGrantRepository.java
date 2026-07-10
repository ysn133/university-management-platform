package com.platform.identityaccess.infrastructure;

import com.platform.identityaccess.domain.AdminPermissionGrant;
import com.platform.identityaccess.domain.PermissionCode;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminPermissionGrantRepository extends JpaRepository<AdminPermissionGrant, UUID> {

    List<AdminPermissionGrant> findByAdminId(UUID adminId);

    boolean existsByAdminIdAndPermissionCode(UUID adminId, PermissionCode permissionCode);

    void deleteByAdminId(UUID adminId);
}
