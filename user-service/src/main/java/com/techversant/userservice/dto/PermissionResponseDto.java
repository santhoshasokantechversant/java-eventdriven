/**
 * @file permissionResponseDto.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 26-08-2025
 * @version 1.0
 * @description This class is for Permission response-Dto
 */
package com.techversant.userservice.dto;

import com.techversant.userservice.utils.enums.PermissionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponseDto {
    private String moduleName;
    private String slugName;
    private List<String> httpMethod;
    private PermissionType permissionType;
    private PermissionType sideNav;
}
