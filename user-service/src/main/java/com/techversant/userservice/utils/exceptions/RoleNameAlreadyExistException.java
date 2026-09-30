/**
 * @file RoleNameAlreadyExistException.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 07,2025
 * @version 1.0
 * @description Exception thrown when an attempt is made to register a role that already exists in the system.
 */

package com.techversant.userservice.utils.exceptions;

public class RoleNameAlreadyExistException extends RuntimeException{
    public RoleNameAlreadyExistException(String message) {
        super(message);
    }
}
