/**
 * @file InvalidInputException.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 07, 2025
 * @version 1.0
 * @description Custom exception thrown when the given input is invalid.
 */

package com.techversant.accountservice.utils.exceptions;

public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
}
