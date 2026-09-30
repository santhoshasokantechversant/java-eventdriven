/**
 * @file PaginatedCustomerResponseDto.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 29,2025
 * @version 1.0
 * @description Dto class for view all customers api
 */

package com.techversant.customer_service.dto;

import com.techversant.customer_service.model.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedCustomerResponseDto {
    private List<Customer> customers;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
}
