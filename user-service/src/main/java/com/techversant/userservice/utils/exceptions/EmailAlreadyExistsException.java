/**
 * @file EmailAlreadyExistsException.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 06,2025
 * @version 1.0
 * @description Exception thrown when an attempt is made to register or update a user with an email address that already exists in the system for an active user.
 */

package com.techversant.userservice.utils.exceptions;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
