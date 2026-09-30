package com.techversant.common_lib.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdatedEvent extends EventBase {
    private Long customerNo;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String status;
    private String updatedAt;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;


}