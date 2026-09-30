/**
 * @file Constants.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 29,2025
 * @version 1.0
 * @description Class containing constant values
 */

package com.techversant.customer_service.utils;

import org.springframework.stereotype.Component;

@Component
public class Constants {

    private Constants() {
    }

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";
    public static final String CUSTOMERS_RETRIEVED = "Customers retrieved successfully.";
    public static final String INVALID_REQUEST_PARAMETERS = "Invalid request parameters.";
    public static final String FAILED_TO_FILTER_USERS = "Failed to filter users.";
    public static final String CUSTOMER_NOT_FOUND = "Customer not found.";
    public static final String CUSTOMER_RETRIEVED = "Customer retrieved.";
    public static final String FAILED_TO_DELETE_CUSTOMER = "Failed to delete customer.";
    public static final String CUSTOMER_DELETED = "Customer deleted successfully.";
    public static final String REGION_NOT_FOUND = "Region not found.";
    public static final String COUNTRIES_RETRIEVED = "Countries retrieved.";
    public static final String COUNTRY_NOT_FOUND = "Country not found.";
    public static final String STATES_RETRIEVED = "States retrieved.";
    public static final String STATE_NOT_FOUND = "State not found.";
    public static final String CITIES_RETRIEVED = "Cities retrieved.";
    public static final String CUSTOMER_CREATED_SUCCESSFULLY = "Customer created successfully.";
    public static final String CUSTOMER_UPDATED_SUCCESSFULLY = "Customer updated successfully.";
    public static final String CREATE = "CREATE";
    public static final String CUSTOMER = "Customer";
    public static final String FAILED = "FAILED";
    public static final String ACTION = "action";
    public static final String EVENT_TYPE = "eventType";
    public static final String STATUS = "status";
    public static final String UPDATE_CUSTOMER = "UPDATE_CUSTOMER: ";
    public static final String CUSTOMER_ID = "customerId";
    public static final String TIMESTAMP = "timestamp";
    public static final String SUCCESS = "SUCCESS";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String USER_ID = "userId";
    public static final String AUTHORIZATION="Authorization";
    public static final String BEARER="Bearer ";
}
