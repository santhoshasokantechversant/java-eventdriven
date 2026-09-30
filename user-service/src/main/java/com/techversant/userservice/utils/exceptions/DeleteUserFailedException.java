/**
 * @file DeleteUserFailedException.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 07,2025
 * @version 1.0
 * @description Exception thrown when user delete fails
 */

package com.techversant.userservice.utils.exceptions;

public class DeleteUserFailedException extends RuntimeException {
    public DeleteUserFailedException(String message) {
        super(message);
    }
}
