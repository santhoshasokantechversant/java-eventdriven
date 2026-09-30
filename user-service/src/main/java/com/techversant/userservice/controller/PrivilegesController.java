package com.techversant.userservice.controller;

import com.techversant.userservice.dto.ApiResponse;
import com.techversant.userservice.dto.PaginatedPrivilegesResponseDto;
import com.techversant.userservice.dto.PrivilegesDto;
import com.techversant.userservice.dto.RolesFilterDto;
import com.techversant.userservice.model.privileges.PrivilegesNewEntity;
import com.techversant.userservice.service.IPrivilegeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.techversant.userservice.utils.Constants.STATUS_ERROR;
import static com.techversant.userservice.utils.Constants.STATUS_SUCCESS;

@RestController
@RequestMapping("/api/v2/privileges")
public class PrivilegesController {
    private final IPrivilegeService iPrivilegeService;

    public PrivilegesController(IPrivilegeService iPrivilegeService) {
        this.iPrivilegeService = iPrivilegeService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PrivilegesNewEntity>> createPrivileges(@RequestBody PrivilegesDto privilegesDto) {
        PrivilegesNewEntity privileges = this.iPrivilegeService.createPrivileges(privilegesDto);
        ApiResponse<PrivilegesNewEntity> apiResponse = new ApiResponse<>();
        if (privileges.getId() == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("Failed to create Privilege.");
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Privilege created successfully.");
        apiResponse.setData(privileges);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<PrivilegesNewEntity>> updatePrivileges(@PathVariable UUID id, @RequestBody PrivilegesDto privilegesDto) {
        PrivilegesNewEntity privileges = this.iPrivilegeService.updatePrivileges(id, privilegesDto);
        ApiResponse<PrivilegesNewEntity> apiResponse = new ApiResponse<>();
        if (privileges.getId() == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("Failed to update Privilege.");
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Privilege updated successfully.");
        apiResponse.setData(privileges);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<ApiResponse<PrivilegesNewEntity>> deletePrivileges(@PathVariable UUID id) {
        PrivilegesNewEntity privileges = this.iPrivilegeService.deletePrivileges(id);
        ApiResponse<PrivilegesNewEntity> apiResponse = new ApiResponse<>();
        if (privileges.getId() == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("Failed to delete Privilege.");
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Privilege deleted successfully.");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PrivilegesNewEntity>> getAPrivilege(@PathVariable UUID id) {
        PrivilegesNewEntity privileges = this.iPrivilegeService.getAPrivilege(id);
        ApiResponse<PrivilegesNewEntity> apiResponse = new ApiResponse<>();
        if (privileges.getId() == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("No privilege found.");
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Privilege found.");
        apiResponse.setData(privileges);
        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/filter-list")
    public ResponseEntity<ApiResponse<PaginatedPrivilegesResponseDto>> listAllPrivileges(@ModelAttribute RolesFilterDto rolesFilterDto) {
        PaginatedPrivilegesResponseDto privileges = this.iPrivilegeService.listAllPrivileges(rolesFilterDto);
        ApiResponse<PaginatedPrivilegesResponseDto> apiResponse = new ApiResponse<>();
        if (privileges.getData().isEmpty()) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("No privileges found.");
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Privileges found.");
        apiResponse.setData(privileges);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<PrivilegesNewEntity>>> listPrivileges() {
        List<PrivilegesNewEntity> privileges = this.iPrivilegeService.listPrivileges();
        ApiResponse<List<PrivilegesNewEntity>> apiResponse = new ApiResponse<>();
        if (privileges== null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("No privileges found.");
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Privileges found.");
        apiResponse.setData(privileges);
        return ResponseEntity.ok(apiResponse);
    }

}
