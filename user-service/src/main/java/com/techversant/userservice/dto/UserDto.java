/**
 * @file UserDto.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 05,2025
 * @version 1.0
 * @description Dto class for adding a new user
 */

package com.techversant.userservice.dto;

import com.techversant.userservice.utils.enums.PermissionType;
import com.techversant.userservice.utils.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be at most 50 characters")
    private String firstName;
    private String  passwordHash;
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    private String lastName;

    @NotBlank(message = "Role ID is required")
    private String roleId;
    private UserType userType;
    private String phoneNumber;
    private String roleName;
    private String keyclockUserId;
    private PermissionType staticUser;
    private LocalDate dateOfBirth;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Long userNo;
    private PermissionType registered = PermissionType.NO;
}
