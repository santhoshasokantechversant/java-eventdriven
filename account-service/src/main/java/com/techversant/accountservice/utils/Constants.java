/**
 * @file Constants.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 06, 2025
 * @version 1.0
 * @description Holds constant values used across the application
 */

package com.techversant.accountservice.utils;

import org.springframework.stereotype.Component;

@Component
public class Constants {
    public static final String STATUS_SUCCESS = "success";
    public static final String ACCOUNT_CREATED = "Account created successfully";
    public static final String ACCOUNT_UPDATED = "Account updated successfully";
    public static final String ACCOUNT_FETCH_SUCCESS_MESSAGE="Account fetched successfully";
    public static final String STATUS_ERROR = "error";
    public static final String STATUS = "status";
    public static final String MESSAGE = "message";
    public static final String ACCOUNT_ALREADY_EXISTS = "Account number already exists";
    public static final String ACCOUNT_CREATED_FAILED = "Failed to create account";
    public static final String ACCOUNT_UPDATED_FAILED = "Account updated failed";
    public static final String DUPLICATE_ACCOUNT_EXCEPTION = "Account with this type already exists for the user.";
    public static final String ACCOUNT_NOT_FOUND = "Account not found";
    public static final String INVALID_CURRENCY_CODE="Invalid currency code";
    public static final String ACCOUNT_SERVICE_GROUP="account-service-group";
    public static final String EARLIEST="earliest";
    public static final String ACCOUNT="Account";
    public static final String SUCCESS="SUCCESS";
    public static final String UPDATE_ACCOUNT="UPDATE_ACCOUNT: ";
    public static final String FAILED="FAILED";
    private Constants() {

    }
}
