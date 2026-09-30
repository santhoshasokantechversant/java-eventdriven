/**
 * @file UpdateRoleFailedException.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 08,2025
 * @version 1.0
 * @description Exception thrown when role update fails
 */

package com.techversant.userservice.utils.exceptions;

public class UpdateRoleFailedException extends RuntimeException{
    public UpdateRoleFailedException(String message){
        super(message);
    }
}
