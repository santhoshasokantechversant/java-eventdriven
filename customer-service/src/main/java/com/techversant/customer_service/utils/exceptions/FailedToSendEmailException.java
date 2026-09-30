/**
 * @file FailedToSendEmailException.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date November 11,2025
 * @version 1.0
 * @description Exception thrown when it fails to send an email
 */

package com.techversant.customer_service.utils.exceptions;

public class FailedToSendEmailException extends RuntimeException{
    public FailedToSendEmailException(String message){
        super(message);
    }
}
