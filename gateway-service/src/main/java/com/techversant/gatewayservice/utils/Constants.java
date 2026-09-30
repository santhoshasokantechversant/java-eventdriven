/**
 * @file Constants.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description Class containing constant values
 */

package com.techversant.gatewayservice.utils;

import org.springframework.stereotype.Component;

@Component
public class Constants {
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";
    public static final String STATUS = "status";
    public static final String MESSAGE = "message";
    public static final String USER_CREATED = "User created successfully.";
    public static final String USER_CREATED_FAILED = "Failed to create User.";

    private Constants() {
    }
}
