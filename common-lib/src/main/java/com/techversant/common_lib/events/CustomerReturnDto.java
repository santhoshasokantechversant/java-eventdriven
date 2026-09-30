package com.techversant.common_lib.events;
/**
 * @file CustomerReturnDto.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date September 28, 2025
 * @version 1.0
 * @description DTO for returning customer details
 */
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerReturnDto {
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
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID userId;
}
