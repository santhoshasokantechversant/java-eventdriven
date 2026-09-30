/**
 * @file GlobalExceptionHandler.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description GlobalExceptionHandler that handles all exceptions
 */

package com.techversant.gatewayservice.config;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.techversant.gatewayservice.dto.ApiResponse;
import com.techversant.gatewayservice.utils.Constants;
import com.techversant.gatewayservice.utils.exceptions.SomethingWentWrongExceptions;
import com.techversant.gatewayservice.utils.exceptions.TokenRequiredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all uncaught exceptions globally within the application.
     * This method intercepts any exception of type Exception that is not handled
     * elsewhere, constructs a standardized ApiResponse with an error status and
     * the exception message, and returns it with HTTP 500 (Internal Server Error).
     *
     * @param ex      the exception that was thrown
     * @param request the current web request during which the exception occurred
     * @return a ResponseEntity containing an ApiResponse with error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleGlobalException(Exception ex, WebRequest request) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles exceptions of type TokenRequiredException.
     * This method constructs a standardized ApiResponse with an error status
     * and the exception message, returning it with HTTP 500 (Internal Server Error).
     * It is triggered whenever a TokenRequiredException is thrown in the application.
     *
     * @param ex the TokenRequiredException that was thrown
     * @return a ResponseEntity containing an ApiResponse with error details
     */
    @ExceptionHandler(TokenRequiredException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleTokenRequiredException(TokenRequiredException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles exceptions of type SomethingWentWrongExceptions.
     * This method constructs a standardized ApiResponse with an error status
     * and the exception message, returning it with HTTP 500 (Internal Server Error).
     * It is triggered whenever a SomethingWentWrongExceptions is thrown in the application.
     *
     * @param ex the SomethingWentWrongExceptions that was thrown
     * @return a ResponseEntity containing an ApiResponse with error details
     */
    @ExceptionHandler(SomethingWentWrongExceptions.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleSomethingWentWrongException(SomethingWentWrongExceptions ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
