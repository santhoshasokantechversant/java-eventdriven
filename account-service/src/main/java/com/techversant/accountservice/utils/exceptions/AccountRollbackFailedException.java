/**
 * @file AccountRollbackFailedException.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date November 11, 2025
 * @version 1.0
 * @description Custom exception thrown when an account rollback fails
 */

package com.techversant.accountservice.utils.exceptions;

public class AccountRollbackFailedException extends RuntimeException{
    public AccountRollbackFailedException(String message){
        super(message);
    }
}
