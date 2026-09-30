/**
 * @file UserUpdateDto.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 07,2025
 * @version 1.0
 * @description Dto for updating user entity
 */

package com.techversant.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDto {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private  String status;
    private String roleId;

}
