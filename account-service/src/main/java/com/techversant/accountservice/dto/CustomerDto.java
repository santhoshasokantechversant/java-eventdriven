/**
 * @file CustomerDto.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date September 25, 2025
 * @version 1.0
 * @description DTO class for customer
 */

package com.techversant.accountservice.dto;

import com.techversant.accountservice.enums.Status;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CustomerDto {

    private UUID customerId;
    private Long customerNo;
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
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID userId;
}
