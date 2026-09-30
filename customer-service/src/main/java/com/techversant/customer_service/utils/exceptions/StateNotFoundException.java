/**
 * @file StateNotFoundException.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date September 02,2025
 * @version 1.0
 * @description Exception thrown when a state is not found
 */

package com.techversant.customer_service.utils.exceptions;

public class StateNotFoundException extends RuntimeException {
    public StateNotFoundException(String message) {
        super(message);
    }
}
