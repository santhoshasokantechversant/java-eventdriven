/**
 * @file FailedToCreateCustomerException.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date November 11,2025
 * @version 1.0
 * @description Exception thrown when it fails to create a customer
 */


package com.techversant.customer_service.utils.exceptions;

public class FailedToCreateCustomerException extends RuntimeException{
    public FailedToCreateCustomerException(String message){
        super(message);
    }
}
