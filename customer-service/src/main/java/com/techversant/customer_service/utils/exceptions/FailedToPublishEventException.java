/**
 * @file FailedToPublishEventException.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date November 11,2025
 * @version 1.0
 * @description Exception thrown when it fails to publish an event
 */

package com.techversant.customer_service.utils.exceptions;

public class FailedToPublishEventException extends RuntimeException{
    public FailedToPublishEventException(String message){
        super(message);
    }
}
