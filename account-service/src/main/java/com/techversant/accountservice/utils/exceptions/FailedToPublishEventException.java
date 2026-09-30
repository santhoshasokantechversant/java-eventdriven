/**
 * @file FailedToPublishEventException.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date November 11, 2025
 * @version 1.0
 * @description Custom exception thrown when an event publishing fails.
 */

package com.techversant.accountservice.utils.exceptions;

public class FailedToPublishEventException extends RuntimeException {
    public FailedToPublishEventException(String message) {
        super(message);
    }
}
