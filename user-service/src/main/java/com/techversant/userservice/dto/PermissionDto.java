/**
 * @file PermissionDto.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 11-08-2025
 * @version 1.0
 * @description This Class is for Permission Dto
 */
package com.techversant.userservice.dto;

import com.techversant.userservice.model.Endpoint;
import com.techversant.userservice.model.Privileges;
import com.techversant.userservice.model.Role;
import com.techversant.userservice.model.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDto {
    @NotNull(message = "Role ID is required")
    private String roleId;
    @NotNull(message = "Privilege ID is required")
    private String privilegeId;
    @NotNull(message = "Endpoint ID is required")
    private String endpointId;
    private String userId;
    private Role role;
    private Privileges privileges;
    private Endpoint endpoint;
    private User user;
}
