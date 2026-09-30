/**
 * @file RoleController.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 07,2025
 * @version 1.0
 * @description Controller to handle apis related to role
 */

package com.techversant.userservice.controller;

import com.google.gson.JsonObject;
import com.techversant.userservice.dto.*;
import com.techversant.userservice.model.Role;
import com.techversant.userservice.service.IRoleService;
import com.techversant.userservice.utils.enums.SortDirection;
import com.techversant.userservice.utils.exceptions.RoleNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.techversant.userservice.utils.Constants.*;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {
    private final IRoleService iRoleService;

    public RoleController(IRoleService iRoleService) {
        this.iRoleService = iRoleService;

    }

    /**
     * Endpoint to create a new role.
     *
     * @param roleDto the DTO containing role information from the request body
     * @return ResponseEntity containing ApiResponse with status, message, and created role (if successful)
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Role>> addRole(@Valid @RequestBody RoleDto roleDto) {
        Role role = this.iRoleService.addRole(roleDto);
        ApiResponse<Role> apiResponse = new ApiResponse<>();
        if (role.getId() == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage(ROLE_CREATED_FAILED);
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(ROLE_CREATED);
        apiResponse.setData(role);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Endpoint to retrieve a paginated and sorted list of roles.
     *
     * @param page          the page number to retrieve (default is 0)
     * @param size          the number of records per page (default is 10)
     * @param sortField     the field to sort the results by (default is "createdAt")
     * @param sortDirection the direction of sorting, either ASC or DESC (default is DESC)
     * @return ResponseEntity containing ApiResponse with status, message, and paginated role data
     */
    @GetMapping("/get-all-roles")
    public ResponseEntity<ApiResponse<PaginatedRoleResponseDto>> getAllRoles(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "createdAt") String sortField, @RequestParam(defaultValue = "DESC") SortDirection sortDirection) {
        Sort sort = sortDirection == SortDirection.ASC ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PaginatedRoleResponseDto paginatedRoleResponseDto = iRoleService.getAllRoles(pageable);
        ApiResponse<PaginatedRoleResponseDto> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(ROLES_RETRIEVED);
        apiResponse.setData(paginatedRoleResponseDto);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Endpoint to retrieve a specific role by its unique identifier.
     *
     * @param id the UUID of the role to retrieve
     * @return ResponseEntity containing ApiResponse with status, message, and role data if found
     * @throws RoleNotFoundException if no role is found with the provided ID
     */
    @GetMapping("/get-role-by-id/{id}")
    public ResponseEntity<ApiResponse<Role>> getRoleById(@PathVariable UUID id){
        Role role=iRoleService.getRoleById(id);
        if(role==null){
            throw new RoleNotFoundException(ROLE_NOT_FOUND);
        }
        ApiResponse<Role> apiResponse=new ApiResponse<>();
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(ROLE_RETRIEVED);
        apiResponse.setData(role);
        return ResponseEntity.ok(apiResponse);
    }

    
    /**
     * Endpoint to filter roles based on various criteria provided in RolesFilterDto.
     *
     * @param rolesFilterDto the DTO containing filter parameters for roles
     * @return ResponseEntity containing ApiResponse with status, message, and filtered paginated role data
     */
    @GetMapping("/filter-roles")
    public ResponseEntity<ApiResponse<PaginatedRoleResponseDto>> filterRoles(@ModelAttribute RolesFilterDto rolesFilterDto){
        PaginatedRoleResponseDto paginatedRoleResponseDto=iRoleService.filterRoles(rolesFilterDto);
        ApiResponse<PaginatedRoleResponseDto> apiResponse=new ApiResponse<>();
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(ROLES_RETRIEVED);
        apiResponse.setData(paginatedRoleResponseDto);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Endpoint to delete a role by its unique identifier.
     *
     * @param id the UUID of the role to be deleted
     * @return ResponseEntity containing ApiResponse with the status and message indicating the result of the operation
     */
    @DeleteMapping("/delete-role/{id}")
    public ResponseEntity<ApiResponse<JsonObject>> deleteRole(@PathVariable UUID id){
        return iRoleService.deleteRole(id);
    }

    /**
     * Endpoint to update an existing role by its unique identifier.
     *
     * @param id the UUID of the role to update
     * @param roleUpdateDto the DTO containing updated role information
     * @return ResponseEntity containing ApiResponse with status, success message, and the updated role
     */
    @PutMapping("/update-role/{id}")
    public ResponseEntity<ApiResponse<Role>> updateRole(@PathVariable UUID id, @RequestBody RoleUpdateDto roleUpdateDto){
        Role role=iRoleService.updateRole(id,roleUpdateDto);
        ApiResponse<Role> apiResponse=new ApiResponse<>();
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(ROLE_UPDATED);
        apiResponse.setData(role);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/fetch-all-roles")
    public ResponseEntity<ApiResponse<List<Role>>> fetchAllRoles() {
        List<Role> roles=iRoleService.fetchAllRoles();
        ApiResponse<List<Role>> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(ROLES_RETRIEVED);
        apiResponse.setData(roles);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/dropdown-list")
    public ResponseEntity<ApiResponse<List<Role>>> dropDown() {
        List<Role> roles=iRoleService.dropDown();
        ApiResponse<List<Role>> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(ROLES_RETRIEVED);
        apiResponse.setData(roles);
        return ResponseEntity.ok(apiResponse);
    }
}
