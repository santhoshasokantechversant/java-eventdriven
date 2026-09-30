/**
 * @file AccountNumberAlreadyExists.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 07, 2025
 * @version 1.0
 * @description Custom exception thrown when an account number already exists in the system.
 */

package com.techversant.accountservice.utils.exceptions;

public class AccountNumberAlreadyExistsException extends RuntimeException {
    public AccountNumberAlreadyExistsException(String message) {
        super(message);
    }
}
