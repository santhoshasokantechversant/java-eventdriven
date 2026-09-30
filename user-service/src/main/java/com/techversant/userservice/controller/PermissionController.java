package com.techversant.userservice.controller;

import com.techversant.userservice.dto.ApiResponse;
import com.techversant.userservice.dto.SaveRolePermissionDto;
import com.techversant.userservice.dto.UpdatePermissionDto;
import com.techversant.userservice.model.privileges.PermissionsNewEntity;
import com.techversant.userservice.service.IPermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.techversant.userservice.utils.Constants.STATUS_ERROR;
import static com.techversant.userservice.utils.Constants.STATUS_SUCCESS;

@RestController
@RequestMapping("/api/v2/privileges/permission")
public class PermissionController {
    private final IPermissionService iPermissionService;
    public PermissionController(IPermissionService iPermissionService) {
        this.iPermissionService = iPermissionService;
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<PermissionsNewEntity>> updatePermissions(@PathVariable UUID id, @RequestBody UpdatePermissionDto updatePermissionDto) {
        PermissionsNewEntity permission = this.iPermissionService.updatePermissions(id, updatePermissionDto);
        ApiResponse<PermissionsNewEntity> apiResponse = new ApiResponse<>();
        if (permission.getId() == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("Failed to update permission.");
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Permission updated successfully.");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/get-permission/{id}")
    public ResponseEntity<ApiResponse<PermissionsNewEntity>> getPermissions(@PathVariable UUID id) {
        PermissionsNewEntity permission = this.iPermissionService.getPermissions(id);
        ApiResponse<PermissionsNewEntity> apiResponse = new ApiResponse<>();
        if (permission.getId() == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("Failed to update permission.");
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Permission updated successfully.");
        apiResponse.setData(permission);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/save-roles/{id}")
    public ResponseEntity<ApiResponse<PermissionsNewEntity>> saveRolesPermissions(@PathVariable UUID id, @RequestBody List<SaveRolePermissionDto> saveRolePermissionDto) {
        List<PermissionsNewEntity> permission = this.iPermissionService.saveRolesPermissions(id, saveRolePermissionDto);
        ApiResponse<PermissionsNewEntity> apiResponse = new ApiResponse<>();
        if (permission == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("Failed to save permission.");
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Permission saved successfully.");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/list-by-roles/{id}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listPermissions(@PathVariable UUID id) {
        List<Map<String, Object>> permission = this.iPermissionService.listPermissions(id);

        ApiResponse<List<Map<String, Object>>> apiResponse = new ApiResponse<>();
        if (permission == null || permission.isEmpty()) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("No Permission found for the role.");
            return ResponseEntity.ok(apiResponse);
        }

        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Permission Found.");
        apiResponse.setData(permission);
        return ResponseEntity.ok(apiResponse);
    }
}
