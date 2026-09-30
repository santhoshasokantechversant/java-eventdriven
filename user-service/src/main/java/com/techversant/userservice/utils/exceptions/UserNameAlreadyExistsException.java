/**
 * @file UserNameAlreadyExistsException.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 06,2025
 * @version 1.0
 * @description Exception thrown when an attempt is made to register or update a user with a username that already exists in the system for an active user.
 */

package com.techversant.userservice.utils.exceptions;

public class UserNameAlreadyExistsException extends RuntimeException {
    public UserNameAlreadyExistsException(String message) {
        super(message);
    }
}
