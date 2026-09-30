/**
 * @file IPrivilegeService.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 11-08-2025
 * @version 1.0
 * @description This interface is for privilage Service
 */
package com.techversant.userservice.service;

import com.techversant.userservice.dto.*;
import com.techversant.userservice.model.Endpoint;
import com.techversant.userservice.model.Permission;
import com.techversant.userservice.model.Privileges;
import com.techversant.userservice.model.privileges.PrivilegesNewEntity;

import java.util.List;
import java.util.UUID;

public interface IPrivilegeService {

    /**
     * Creates and saves a new Privileges entity based on the provided PrivilegesDto.
     *
     * @param privilegesDto the data transfer object containing privilege details
     * @return the newly created Privileges entity
     */
    Privileges addPrivileges(PrivilegesDto privilegesDto);

    /**
     * Creates and saves a new Endpoint entity based on the provided EndpointDto.
     *
     * @param endpointDto the data transfer object containing endpoint details
     * @return the newly created Endpoint entity
     */
    Endpoint addEndPoints(EndpointDto endpointDto);

    /**
     * Creates and saves a new Permission entity based on the provided PermissionDto.
     *
     * @param permissionDto the data transfer object containing permission details
     * @return the newly created Permission entity
     */
    Permission addPermission(PermissionDto permissionDto);

    /**
     * Retrieves a list of active permissions associated with the specified role ID.
     *
     * @param id the UUID of the role whose permissions are to be retrieved
     * @return a list of active Permission objects linked to the given role
     */
    List<Permission> listPermission(UUID id);

    /**
     * Updates the permissions associated with a specific role.
     * <p>
     * This method updates both the local permission records and synchronizes the changes with the associated Keycloak role.
     *
     * @param id the UUID of the role to update permissions for
     * @param permissionDto the list of Permission objects containing the new or updated permissions
     * @return true if the update is successful; false otherwise
     */
    boolean upatePermission(UUID id, List<Permission> permissionDto);

    List<SidenavResponseDto> sideNav(UUID id);
    List<SidenavResponseDto> sideNavNew(UUID id);
    List<SideNavDTO> listSideNav();

    Permission updatePermission(UUID id, Permission permission);

    Permission getPermission(UUID id);

    PrivilegesNewEntity createPrivileges(PrivilegesDto privilegesDto);

    PrivilegesNewEntity updatePrivileges(UUID id, PrivilegesDto privilegesDto);
    PrivilegesNewEntity deletePrivileges(UUID id);
    PrivilegesNewEntity getAPrivilege(UUID id);
    PaginatedPrivilegesResponseDto listAllPrivileges(RolesFilterDto rolesFilterDto);
    List<PrivilegesNewEntity> listPrivileges();
    void createPermissions(PrivilegesNewEntity privilege);
    void updatePermissions(PrivilegesNewEntity privilege);
}
