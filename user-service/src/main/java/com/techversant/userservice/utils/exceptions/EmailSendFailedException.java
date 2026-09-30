package com.techversant.userservice.utils.exceptions;

public class EmailSendFailedException extends RuntimeException{

    public EmailSendFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
