/**
 * @file RoleDto.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 07,2025
 * @version 1.0
 * @description Dto class for adding new role
 */

package com.techversant.userservice.dto;

import com.techversant.userservice.utils.enums.PermissionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {
    @NotBlank(message = "Role name is required")
    @Size(min = 3, max = 50, message = "Role name must be between 1 and 50 characters")
    private String name;
    private String keyclockRoleId;
    private String description;
    private String position;
    private PermissionType roleCreate;
    private UUID hierarchy;
}
