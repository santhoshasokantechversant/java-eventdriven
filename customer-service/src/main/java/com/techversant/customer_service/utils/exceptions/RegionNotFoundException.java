/**
 * @file RegionNotFoundException.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date September 02,2025
 * @version 1.0
 * @description Exception thrown when a region is not found
 */

package com.techversant.customer_service.utils.exceptions;

public class RegionNotFoundException extends RuntimeException {
    public RegionNotFoundException(String message) {
        super(message);
    }
}
