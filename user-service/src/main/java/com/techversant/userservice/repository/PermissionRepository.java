/**
 * @author Nihal Elton John
 * @version 1.0
 * @file PermissionRepository.java
 * @company Techversant Infotech
 * @date 11-08-2025
 * @description This Interface is for permission Repository
 */

package com.techversant.userservice.repository;

import com.techversant.userservice.model.*;
import com.techversant.userservice.utils.enums.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    /**
     * Finds an active or inactive Permission by role, privileges, endpoint, user, and active status.
     *
     * @param role       the Role entity associated with the Permission
     * @param privileges the Privileges entity associated with the Permission
     * @param endpoint   the Endpoint entity associated with the Permission
     * @param user       the User entity associated with the Permission
     * @param isActive   the active status to filter the Permission
     * @return the Permission entity matching the given criteria, or null if none found
     */
    Permission findOneByRoleAndPrivilegesAndEndpointAndUserAndIsActive(Role role, Privileges privileges, Endpoint endpoint, User user, boolean isActive);

    /**
     * Finds an active or inactive Permission by role, privileges, endpoint, and active status.
     *
     * @param role       the Role entity associated with the Permission
     * @param privileges the Privileges entity associated with the Permission
     * @param endpoint   the Endpoint entity associated with the Permission
     * @param isActive   the active status to filter the Permission
     * @return the Permission entity matching the given criteria, or null if none found
     */
    Permission findOneByRoleAndPrivilegesAndEndpointAndIsActive(Role role, Privileges privileges, Endpoint endpoint, boolean isActive);

    /**
     * Retrieves all permissions associated with a given role and active status.
     *
     * @param role     the Role entity to filter permissions by
     * @param isActive a boolean indicating whether to fetch only active (true) or inactive (false) permissions
     * @return a list of Permission objects matching the specified role and active status
     */
    List<Permission> findAllByRoleAndIsActive(Role role, boolean isActive);


    List<Permission> findAllByRoleAndPermissionTypeAndIsActive(Role role, PermissionType permissionType, boolean isActive);


    Permission findByIdAndIsActive(UUID id, boolean isActive);

    List<Permission>findAllByRoleAndPrivilegesAndIsActive(Role role, Privileges privileges, boolean isActive);
}
