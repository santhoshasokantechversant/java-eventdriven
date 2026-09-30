/**
 * @file PaginatedAccountResponseDto.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 08, 2025
 * @version 1.0
 * @description DTO class for transferring paginated account data in response.
 */

package com.techversant.accountservice.dto;

import com.techversant.accountservice.model.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedAccountResponseDto {
    private List<Account> account;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
}
