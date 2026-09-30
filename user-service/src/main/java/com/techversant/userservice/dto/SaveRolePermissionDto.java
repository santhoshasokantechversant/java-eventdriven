package com.techversant.userservice.dto;

import com.techversant.userservice.utils.enums.PermissionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class SaveRolePermissionDto {
    private UUID id;
    private PermissionType permissionType;

}
