package com.techversant.userservice.utils.exceptions;

public class ForgotPasswordEmailException extends RuntimeException{

    public ForgotPasswordEmailException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForgotPasswordEmailException(String message) {
        super(message);
    }
}
