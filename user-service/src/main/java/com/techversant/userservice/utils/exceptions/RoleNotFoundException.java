/**
 * @file RoleNotFoundException.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 08,2025
 * @version 1.0
 * @description Exception thrown when a role is not found
 */

package com.techversant.userservice.utils.exceptions;

public class RoleNotFoundException extends RuntimeException{
    public RoleNotFoundException(String message){
        super(message);
    }
}
