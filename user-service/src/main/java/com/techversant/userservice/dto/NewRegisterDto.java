/**
 * @file NewRegisterDto.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 27-08-2025
 * @version 1.0
 * @description This class is for to Register new user
 */
package com.techversant.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewRegisterDto {
    private String emailId;
    private String password;
    private String confirmPassword;
}
