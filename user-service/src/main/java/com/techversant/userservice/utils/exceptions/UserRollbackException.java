package com.techversant.userservice.utils.exceptions;

public class UserRollbackException extends RuntimeException{

    public UserRollbackException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserRollbackException(String message) {
        super(message);
    }
}
