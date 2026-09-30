/**
 * @file CustomerRollbackFailedException.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date November 11,2025
 * @version 1.0
 * @description Exception thrown when customer rollback fails
 */

package com.techversant.customer_service.utils.exceptions;

public class CustomerRollbackFailedException extends RuntimeException{
    public CustomerRollbackFailedException(String message){
        super(message);
    }
}
