/**
 * @file UserDisplayDto.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 06,2025
 * @version 1.0
 * @description Dto class for user response
 */

package com.techversant.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDisplayDto {

    private UUID id;
    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long userNo;
    private LocalDate dateOfBirth;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phoneNumber;
    private boolean isActive;
    private UUID roleId;
}
