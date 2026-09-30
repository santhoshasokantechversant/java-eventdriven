/**
 * @file AccountDto.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 05, 2025
 * @version 1.0
 * @description Dto class for transferring data when creating a new Account
 */

package com.techversant.accountservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    @NotNull(message = "Customer no cannot be null")
    private Long customerNo;

    private Long accNo;

    @NotBlank(message = "Account number cannot be null")
    @Size(min = 11, max = 11, message = "Account number must be exactly 11 digits")
    @Pattern(regexp = "\\d{11}", message = "Account number must contain only digits")
    private String accountNumber;

    @NotBlank(message = "Account type is required")
    private String accountType;

    @NotNull(message = "Balance cannot be null")
    @DecimalMin(value = "0.00", inclusive = true, message = "Balance must be greater than or equal to 0.00")
    @Digits(integer = 10, fraction = 2, message = "Balance must be a valid amount with up to 2 decimal places")
    private BigDecimal balance = BigDecimal.valueOf(0.00);

    @NotBlank(message = "Currency is required")
    private String currency;

    private UUID customerId;
    private UUID userId;
}
