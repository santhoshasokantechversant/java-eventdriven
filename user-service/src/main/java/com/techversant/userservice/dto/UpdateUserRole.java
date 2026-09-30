/**
 * @file UpdateUserRole.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 29-08-2025
 * @version 1.0
 * @description This class is to update the user's role
 */
package com.techversant.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRole {
    private String roleId;
    private String roleName;

}
