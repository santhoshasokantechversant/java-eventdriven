/**
 * @file FilterCustomerRequestDto.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 29,2025
 * @version 1.0
 * @description Dto class for filtering customers based on different criteria
 */

package com.techversant.customer_service.dto;

import com.techversant.customer_service.utils.enums.Status;
import com.techversant.customer_service.utils.enums.ViewAllCustomersSortFields;
import lombok.Data;
import org.hibernate.query.SortDirection;

@Data
public class FilterCustomerRequestDto {
    private String firstName = null;
    private String lastName = null;
    private String email = null;
    private String phoneNumber = null;
    private String address = null;
    private String city = null;
    private String state = null;
    private String postalCode = null;
    private String country = null;
    private Status status = Status.ACTIVE;
    private int page = 0;
    private int size = 10;
    private ViewAllCustomersSortFields sortField = ViewAllCustomersSortFields.CREATED_AT;
    private SortDirection sortDirection = SortDirection.DESCENDING;
}
