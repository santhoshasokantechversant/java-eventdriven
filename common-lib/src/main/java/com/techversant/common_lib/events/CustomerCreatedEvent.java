package com.techversant.common_lib.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreatedEvent extends EventBase {
    private Long customerNo;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String status;
    private String createdAt;
    private UUID customerId;
    private LocalDate dateOfBirth;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String correlationId;

}