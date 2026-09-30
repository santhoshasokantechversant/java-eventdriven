/**
 * @file CustomerNotFoundException.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 29,2025
 * @version 1.0
 * @description Exception thrown when a customer is not found
 */

package com.techversant.customer_service.utils.exceptions;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
