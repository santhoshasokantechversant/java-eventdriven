/**
 * @file CustomerEmailDto.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description Dto class for customer email
 */

package com.techversant.customer_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEmailDto {
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private String phoneNumber;
    private String accountNumber;
    private String accountType;
}
