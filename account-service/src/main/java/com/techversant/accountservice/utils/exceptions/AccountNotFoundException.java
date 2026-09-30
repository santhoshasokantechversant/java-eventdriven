/**
 * @file AccountNotFoundException.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 07, 2025
 * @version 1.0
 * @description Custom exception thrown when a requested account is not found in the system.
 */

package com.techversant.accountservice.utils.exceptions;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
