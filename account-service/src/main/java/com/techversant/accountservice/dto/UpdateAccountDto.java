/**
 * @file UpdateAccountDto.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 07, 2025
 * @version 1.0
 * @description Dto class for transferring data when updating an Account
 */

package com.techversant.accountservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountDto {
    private String accountType;
    @DecimalMin(value = "0.00", inclusive = true, message = "Balance must be greater than or equal to 0.00")
    @Digits(integer = 10, fraction = 2, message = "Balance must be a valid amount with up to 2 decimal places")
    private BigDecimal balance;
    private String currency;
    private  String status;
}
