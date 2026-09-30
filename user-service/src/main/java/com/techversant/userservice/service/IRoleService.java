/**
 * @file IRoleService.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 07,2025
 * @version 1.0
 * @description Service interface for handling operations related to role entity
 */

package com.techversant.userservice.service;

import com.google.gson.JsonObject;
import com.techversant.userservice.dto.*;
import com.techversant.userservice.model.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;


public interface IRoleService {

    /**
     * Adds a new role to the system.
     *
     * @param roleDto the data transfer object containing role details (e.g. name, description)
     * @return the saved Role entity
     */
    Role addRole(RoleDto roleDto);

    /**
     * Retrieves a paginated list of active roles based on the provided pagination and sorting details.
     *
     * @param pageable the pagination and sorting information
     * @return PaginatedRoleResponseDto containing active roles and pagination metadata
     */
    PaginatedRoleResponseDto getAllRoles(Pageable pageable);

    /**
     * Retrieves an active role by its unique identifier.
     *
     * @param id the UUID of the role to retrieve
     * @return the Role entity if found and active; otherwise, null
     */
    Role getRoleById(UUID id);

    /**
     * Retrieves a paginated list of roles filtered according to the criteria specified in RolesFilterDto.
     *
     * @param rolesFilterDto the filter criteria including pagination, sorting, and role-specific filters
     * @return PaginatedRoleResponseDto containing the filtered roles and pagination metadata
     */
    PaginatedRoleResponseDto filterRoles(RolesFilterDto rolesFilterDto);

    /**
     * Performs a soft delete of a role by its unique identifier.
     *
     * @param id the UUID of the role to delete
     * @return ResponseEntity containing ApiResponse with status and deletion message
     */
    ResponseEntity<ApiResponse<JsonObject>> deleteRole(UUID id);

    /**
     * Updates an active role identified by the given UUID using the provided role update data.
     *
     * @param id the UUID of the role to update
     * @param roleUpdateDto the DTO containing fields to update (e.g., name, description)
     * @return the updated Role entity
     */
    Role updateRole(UUID id, RoleUpdateDto roleUpdateDto);

    /**
     * Retrieves a Role entity by its unique identifier.
     *
     * @param id the UUID of the Role to retrieve
     * @return the Role entity matching the provided ID
     */
    Role roleById(UUID id);

    List<Role> fetchAllRoles();
    List<Role> dropDown();
}
