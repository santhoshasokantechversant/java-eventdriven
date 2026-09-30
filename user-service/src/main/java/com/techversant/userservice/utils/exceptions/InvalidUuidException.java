/**
 * @file InvalidUuidException.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 12-08-2025
 * @version 1.0
 * @description This class is for Invalid Uuid exception
 */
package com.techversant.userservice.utils.exceptions;

public class InvalidUuidException extends RuntimeException{
    public InvalidUuidException(String message) {
        super(message);
    }
}
