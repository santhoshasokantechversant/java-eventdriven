/**
 * @file LoginReturnDto.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 08,2025
 * @version 1.0
 * @description Dto class for login response
 */

package com.techversant.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginReturnDto {
    private TokenDto token;
    private String userName;
    private String fullName;
    private String emailId;
    private UUID id;
    private String role;
    private UUID roleId;
    private List<SidenavResponseDto> sidenav;
}
