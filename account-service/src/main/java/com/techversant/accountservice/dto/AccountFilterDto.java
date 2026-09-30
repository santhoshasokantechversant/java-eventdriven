/**
 * @file AccountFilterDto.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 08, 2025
 * @version 1.0
 * @description DTO class for transferring account filter criteria for search operations.
 */

package com.techversant.accountservice.dto;

import com.techversant.accountservice.enums.AccountType;
import com.techversant.accountservice.enums.SortDirection;
import com.techversant.accountservice.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountFilterDto {
    private String accountNumber = null;
    private AccountType accountType = null;
    private Integer currencyId = null;
    private Long customerNo=null;
    private Status status =null;
    private int page = 0;
    private int size = 10;
    private String sortField = "accNo";
    private SortDirection sortDirection = SortDirection.DESC;
}
