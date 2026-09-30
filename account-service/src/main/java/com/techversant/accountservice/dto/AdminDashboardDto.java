/**
 * @file AdminDashboardDto.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description DTO class for admin dashboard response
 */

package com.techversant.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDto {
    private Integer customerCount;
    private BigDecimal totalAmount;
    private Integer totalAccounts;
    private BigDecimal totalClosedAccounts;
    private BigDecimal activeAccounts;
    private BigDecimal inactiveAccounts;
}
