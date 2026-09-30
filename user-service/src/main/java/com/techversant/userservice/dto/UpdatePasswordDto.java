/**
 * @file UpdatePasswordDto.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 29-08-2025
 * @version 1.0
 * @description This class is for to update password
 */
package com.techversant.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePasswordDto {
    private String oldPassword;
    private String newPassword;
    private String confirmNewPassword;

}
