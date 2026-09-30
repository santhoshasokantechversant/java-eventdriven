/**
 * @file PermissionKeyclockDto.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 14-08-2025
 * @version 1.0
 * @description Dto class for permission for keyclock
 */
package com.techversant.userservice.dto;

import com.techversant.userservice.utils.enums.PermissionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionKeyclockDto {
    private String module;
    private String endpoint;
    private String method;
    private PermissionType permission;
}
