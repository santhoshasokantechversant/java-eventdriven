package com.techversant.userservice.repository.privilegesnew;

import com.techversant.userservice.model.privileges.PermissionsNewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionRepositorynew extends JpaRepository<PermissionsNewEntity, UUID> {
    List<PermissionsNewEntity> findAllByPrivilegeId(UUID privilegeId);
    List<PermissionsNewEntity> findAllByEndpointId(UUID endpointId);
    List<PermissionsNewEntity> findAllByRoleId(UUID roleId);
    List<PermissionsNewEntity> findAllByPrivilegeIdAndRoleId(UUID privilegeId, UUID roleId);
}
