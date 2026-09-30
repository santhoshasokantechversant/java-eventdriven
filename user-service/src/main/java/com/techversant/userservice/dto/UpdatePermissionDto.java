package com.techversant.userservice.dto;

import com.techversant.userservice.utils.enums.PermissionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePermissionDto {
    private String backendUrl;
    private String httpMethod;
    private UUID roleId;
    private UUID userId;
    private UUID privilegeId;
    private UUID endpointId;
    private String endpointName;
    private String privilegeName;
    private String slugName;
    private Integer position;
    private PermissionType permissionType;
    private PermissionType sideNav;
    private PermissionType updatableEndpoint;
    private PermissionType updatableSlug;
}
