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

import java.math.BigDecimal;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountsReturnDto {
    private UUID id;
    private Long customerNo;
    private Long accNo;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private UUID customerId;
    private boolean isActive;
    private String status;
}
