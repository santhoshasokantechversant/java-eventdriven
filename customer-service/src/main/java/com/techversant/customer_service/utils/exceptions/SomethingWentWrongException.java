/**
 * @file SomethingWentWrongException.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description Custom runtime exception indicating that an unexpected error occurred during application processing.
 */

package com.techversant.customer_service.utils.exceptions;

public class SomethingWentWrongException extends RuntimeException{
    public SomethingWentWrongException(String message) {
        super(message);
    }

    public SomethingWentWrongException(String message, Throwable cause) {
        super(message, cause);
    }
}
