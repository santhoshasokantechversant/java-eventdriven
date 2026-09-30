/**
 * @file UpdateUserFailedException.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 07,2025
 * @version 1.0
 * @description Exception thrown when user update fails
 */

package com.techversant.userservice.utils.exceptions;

public class UpdateUserFailedException extends RuntimeException {

    public UpdateUserFailedException(String message) {
        super(message);
    }
}
