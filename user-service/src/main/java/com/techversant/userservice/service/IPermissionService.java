package com.techversant.userservice.service;

import com.techversant.userservice.dto.SaveRolePermissionDto;
import com.techversant.userservice.dto.UpdatePermissionDto;
import com.techversant.userservice.model.privileges.PermissionsNewEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IPermissionService {
    PermissionsNewEntity updatePermissions(UUID id, UpdatePermissionDto updatePermissionDto);
    List<PermissionsNewEntity> saveRolesPermissions(UUID id, List<SaveRolePermissionDto> saveRolePermissionDto);
    List<Map<String, Object>> listPermissions(UUID id);
    PermissionsNewEntity getPermissions(UUID id);
}
