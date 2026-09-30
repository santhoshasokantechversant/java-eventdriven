package com.techversant.userservice.utils.exceptions;

public class RollbackFailureException extends RuntimeException{

    public RollbackFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public RollbackFailureException(String message) {
        super(message);
    }
}
