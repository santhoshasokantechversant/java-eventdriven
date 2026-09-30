/**
 * @file FailedToFilterCustomersException.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 29,2025
 * @version 1.0
 * @description Exception thrown when an error occurs while filtering users
 */

package com.techversant.customer_service.utils.exceptions;

public class FailedToFilterCustomersException extends RuntimeException {
    public FailedToFilterCustomersException(String message) {
        super(message);
    }
}
