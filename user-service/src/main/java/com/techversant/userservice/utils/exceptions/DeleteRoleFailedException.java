/**
 * @file DeleteRoleFailedException.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 08,2025
 * @version 1.0
 * @description Exception thrown when role delete fails
 */

package com.techversant.userservice.utils.exceptions;

public class DeleteRoleFailedException extends RuntimeException{
    public DeleteRoleFailedException(String message){
        super(message);
    }
    public DeleteRoleFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
