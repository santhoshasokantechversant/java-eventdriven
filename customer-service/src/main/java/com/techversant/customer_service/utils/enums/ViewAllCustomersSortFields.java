/**
 * @file ViewAllCustomersSortFields.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 29,2025
 * @version 1.0
 * @description Enum to store permissible value of sort fields
 */

package com.techversant.customer_service.utils.enums;

import lombok.Getter;

@Getter
public enum ViewAllCustomersSortFields {
    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    EMAIL("email"),
    CREATED_AT("createdAt");

    private final String sortField;

    ViewAllCustomersSortFields(String sortField) {
        this.sortField = sortField;
    }

}
