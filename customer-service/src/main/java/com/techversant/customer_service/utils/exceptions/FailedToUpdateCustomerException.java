/**
 * @file FailedToUpdateCustomerException.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date November 11,2025
 * @version 1.0
 * @description Exception thrown when updating a customer fails
 */

package com.techversant.customer_service.utils.exceptions;

public class FailedToUpdateCustomerException extends RuntimeException{
    public FailedToUpdateCustomerException(String message){
        super(message);
    }
}
