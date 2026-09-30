/**
 * @file DuplicateAccountException.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 07, 2025
 * @version 1.0
 * @description Custom exception thrown when the same user already has an account with the same account type.
 */

package com.techversant.accountservice.utils.exceptions;

public class DuplicateAccountException extends RuntimeException {
    public DuplicateAccountException(String message) {
        super(message);
    }
}
