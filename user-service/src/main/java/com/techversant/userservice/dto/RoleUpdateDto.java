/**
 * @file RoleUpdateDto.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 08,2025
 * @version 1.0
 * @description Dto for updating role entity
 */

package com.techversant.userservice.dto;

import com.techversant.userservice.utils.enums.PermissionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleUpdateDto {
    private String name;
    private String description;
    private PermissionType roleCreate;
}
