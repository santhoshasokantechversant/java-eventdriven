/**
 * @file FailedToDeleteCustomerException.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date September 01,2025
 * @version 1.0
 * @description Exception thrown when it fails to delete a customer
 */

package com.techversant.customer_service.utils.exceptions;

public class FailedToDeleteCustomerException extends RuntimeException {
    public FailedToDeleteCustomerException(String message) {
        super(message);
    }
}
