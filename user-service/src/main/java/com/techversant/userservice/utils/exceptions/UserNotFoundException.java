/**
 * @file UserNotFoundException.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 06,2025
 * @version 1.0
 * @description Exception thrown when no user is found in the system
 */

package com.techversant.userservice.utils.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
