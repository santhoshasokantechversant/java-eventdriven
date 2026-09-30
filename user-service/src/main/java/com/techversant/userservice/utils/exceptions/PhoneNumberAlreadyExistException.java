package com.techversant.userservice.utils.exceptions;

public class PhoneNumberAlreadyExistException extends RuntimeException{
    public PhoneNumberAlreadyExistException(String message) {
        super(message);
    }
}
