/**
 * @file SomethingWentWrongExceptions.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description Custom exception thrown when an exception occurs.
 */

package com.techversant.accountservice.utils.exceptions;

public class SomethingWentWrongExceptions extends RuntimeException{
    public SomethingWentWrongExceptions(String message) {
        super(message);
    }
}
