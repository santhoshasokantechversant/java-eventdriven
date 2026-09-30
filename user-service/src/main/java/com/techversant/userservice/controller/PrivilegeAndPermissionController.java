/**
 * @file PrivilegeAndPermissionController.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 11-08-2025
 * @version 1.0
 * @description This class is a Controller class for Authentication and Authorization
 */
package com.techversant.userservice.controller;

import com.techversant.userservice.dto.*;
import com.techversant.userservice.model.Endpoint;
import com.techversant.userservice.model.Permission;
import com.techversant.userservice.model.Privileges;
import com.techversant.userservice.service.IPrivilegeService;
import com.techversant.userservice.utils.Constants;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.techversant.userservice.utils.Constants.STATUS_ERROR;
import static com.techversant.userservice.utils.Constants.STATUS_SUCCESS;

@RestController
@RequestMapping("/api/v1/privileges-permissions")
public class PrivilegeAndPermissionController {
    private final IPrivilegeService iPrivilegeService;

    public PrivilegeAndPermissionController(IPrivilegeService iPrivilegeService) {
        this.iPrivilegeService = iPrivilegeService;
    }

    /**
     * Endpoint to add new privileges.
     *
     * @param privilegesDto the data transfer object containing privilege details to be added
     * @return ResponseEntity containing ApiResponse with success or error status, message, and the created Privileges object if successful
     */
    @PostMapping("/add-privileges")
    public ResponseEntity<ApiResponse<Privileges>> addPrivileges(@Valid @RequestBody PrivilegesDto privilegesDto) {
        Privileges privileges = this.iPrivilegeService.addPrivileges(privilegesDto);
        ApiResponse<Privileges> apiResponse = new ApiResponse<>();
        if (privileges.getId() == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage(Constants.PRIVILEGE_CREATED_FAILED);
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(Constants.PRIVILEGE_CREATED);
        apiResponse.setData(privileges);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Endpoint to add new API endpoints as privileges.
     *
     * @param endpointDto the data transfer object containing endpoint details to be added
     * @return ResponseEntity containing ApiResponse with success or error status, message, and the created Endpoint object if successful
     */
    @PostMapping("/add-endpoints")
    public ResponseEntity<ApiResponse<Endpoint>> addEndpoints(@Valid @RequestBody EndpointDto endpointDto) {
        Endpoint endpoint = this.iPrivilegeService.addEndPoints(endpointDto);
        ApiResponse<Endpoint> apiResponse = new ApiResponse<>();
        if (endpoint.getId() == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage(Constants.PRIVILEGE_CREATED_FAILED);
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(Constants.PRIVILEGE_CREATED);
        apiResponse.setData(endpoint);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Endpoint to add a new permission.
     *
     * @param permissionDto the data transfer object containing permission details to be added
     * @return ResponseEntity containing ApiResponse with success or error status, message, and the created Permission object if successful
     */
    @PostMapping("/add-permission")
    public ResponseEntity<ApiResponse<Permission>> addPermission(@Valid @RequestBody PermissionDto permissionDto) {
        Permission permission = this.iPrivilegeService.addPermission(permissionDto);
        ApiResponse<Permission> apiResponse = new ApiResponse<>();
        if (permission.getId() == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage(Constants.PRIVILEGE_CREATED_FAILED);
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(Constants.PRIVILEGE_CREATED);
        apiResponse.setData(permission);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Endpoint to update permissions for a given ID.
     *
     * @param id          the UUID of the entity whose permissions are to be updated
     * @param permissions the list of Permission objects containing updated permission details
     * @return ResponseEntity containing ApiResponse with success or error status, message, and no data payload
     */
    @PutMapping("/update-permission/{id}")
    public ResponseEntity<ApiResponse<Permission>> updatePermission(@PathVariable UUID id, @Valid @RequestBody List<Permission> permissions) {
        boolean permission = this.iPrivilegeService.upatePermission(id, permissions);
        ApiResponse<Permission> apiResponse = new ApiResponse<>();
        if (!permission) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage(Constants.PRIVILEGE_CREATED_FAILED);
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(Constants.PRIVILEGE_CREATED);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Endpoint to retrieve the list of permissions associated with a given ID.
     *
     * @param id the UUID of the entity for which permissions are to be retrieved
     * @return ResponseEntity containing ApiResponse with success or error status, message,
     * and a list of Permission objects if found
     */
    @GetMapping("/list-permission/{id}")
    public ResponseEntity<ApiResponse<List<Permission>>> listPermission(@PathVariable UUID id) {
        List<Permission> permissions = iPrivilegeService.listPermission(id);

        ApiResponse<List<Permission>> apiResponse = new ApiResponse<>();

        if (permissions.isEmpty()) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("No permissions found for given ID");
            return ResponseEntity.ok(apiResponse);
        }

        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Permissions fetched successfully");
        apiResponse.setData(permissions);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/side-nav/{id}")
    public ResponseEntity<ApiResponse<List<SidenavResponseDto>>> sideNav(@PathVariable UUID id) {
        List<SidenavResponseDto> sideNavs = iPrivilegeService.sideNav(id);

        ApiResponse<List<SidenavResponseDto>> apiResponse = new ApiResponse<>();

        if (sideNavs.isEmpty()) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("No Side-nav found");
            return ResponseEntity.ok(apiResponse);
        }

        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Side-nav fetched successfully");
        apiResponse.setData(sideNavs);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/list-side-nav")
    public ResponseEntity<ApiResponse<List<SideNavDTO>>> listSideNav() {
        List<SideNavDTO> sideNavs = iPrivilegeService.listSideNav();

        ApiResponse<List<SideNavDTO>> apiResponse = new ApiResponse<>();

        if (sideNavs.isEmpty()) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("No Side-nav found");
            return ResponseEntity.ok(apiResponse);
        }

        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Side-nav fetched successfully");
        apiResponse.setData(sideNavs);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/update-slug/{id}")
    public ResponseEntity<ApiResponse<Permission>> updatePermission(@PathVariable UUID id, @Valid @RequestBody Permission permissions) {
        Permission per = iPrivilegeService.updatePermission(id, permissions);

        ApiResponse<Permission> apiResponse = new ApiResponse<>();

        if (per.getId() == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("Failed to update Permission.");
            return ResponseEntity.ok(apiResponse);
        }

        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Successfully updated Permission.");
        apiResponse.setData(per);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/get-permission/{id}")
    public ResponseEntity<ApiResponse<Permission>> getPermission(@PathVariable UUID id) {
        Permission per = iPrivilegeService.getPermission(id);

        ApiResponse<Permission> apiResponse = new ApiResponse<>();

        if (per.getId() == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("No permission Found.");
            return ResponseEntity.ok(apiResponse);
        }

        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Permission fetched successfully.");
        apiResponse.setData(per);
        return ResponseEntity.ok(apiResponse);
    }


}
