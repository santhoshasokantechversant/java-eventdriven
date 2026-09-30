/**
 * @file TokenRequiredException.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description Class for token required exception
 */

package com.techversant.gatewayservice.utils.exceptions;

public class TokenRequiredException extends RuntimeException {
    public TokenRequiredException(String massage) {
        super(massage);
    }
}
