/**
 * @file SomethingWentWrongException.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 07,2025
 * @version 1.0
 * @description Exception thrown when an error occurs .
 */


package com.techversant.userservice.utils.exceptions;

public class SomethingWentWrongException extends RuntimeException{
    public SomethingWentWrongException(String message) {
        super(message);
    }
    public SomethingWentWrongException(String message, Throwable cause) {
        super(message, cause);
    }
}
