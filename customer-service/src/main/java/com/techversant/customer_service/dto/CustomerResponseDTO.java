/**
 * @file CustomerResponseDTO.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description Dto class for customer response
 */

package com.techversant.customer_service.dto;

import com.techversant.customer_service.utils.enums.Status;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CustomerResponseDTO {
    private UUID customerId;
    private Long customerNo;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optional fields
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private UUID userId;
}