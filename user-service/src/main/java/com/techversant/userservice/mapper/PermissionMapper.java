/**
 * @file PermissionMapper.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 11-08-2025
 * @version 1.0
 * @description This  Class is for Permission Mapper
 */
package com.techversant.userservice.mapper;

import com.techversant.userservice.model.*;
import com.techversant.userservice.utils.enums.PermissionType;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    /**
     * Maps user, endpoint, privileges, and role to an existing Permission entity.
     *
     * @param user       the User entity associated with the permission
     * @param endpoint   the Endpoint entity associated with the permission
     * @param privileges the Privileges entity associated with the permission
     * @param role       the Role entity associated with the permission
     * @param permission the existing Permission entity to be updated
     * @return the updated Permission entity with assigned user, endpoint, privileges, and role
     */
    public Permission permissionDtoToEntity(User user, Endpoint endpoint, Privileges privileges, Role role, Permission permission){
        permission.setUser(user);
        permission.setEndpoint(endpoint);
        permission.setRole(role);
        permission.setPrivileges(privileges);
        return permission;
    }

    /**
     * Maps endpoint, privileges, and role to an existing Permission entity and sets the permission type to YES.
     *
     * @param endpoint   the Endpoint entity associated with the permission
     * @param privileges the Privileges entity associated with the permission
     * @param role       the Role entity associated with the permission
     * @param permission the existing Permission entity to be updated
     * @return the updated Permission entity with assigned endpoint, privileges, role, and permission type set to YES
     */
    public Permission permissionDtoToEntity(Endpoint endpoint, Privileges privileges, Role role, Permission permission){
        permission.setEndpoint(endpoint);
        permission.setRole(role);
        permission.setPrivileges(privileges);
        permission.setPermissionType(PermissionType.YES);
        return permission;
    }
}
