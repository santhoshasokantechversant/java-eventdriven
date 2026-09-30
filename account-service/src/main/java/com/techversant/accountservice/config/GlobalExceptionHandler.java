/**
 * @file GlobalExceptionHandler.java
 * @company Techversant Infotech
 * @author V.Antelson
 * @date August 06, 2025
 * @version 1.0
 * @description Handles exceptions globally in the application
 */

package com.techversant.accountservice.config;

import com.google.gson.JsonObject;
import com.techversant.accountservice.dto.ApiResponse;
import com.techversant.accountservice.utils.Constants;
import com.techversant.accountservice.utils.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;


@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all uncaught exceptions thrown within the application.
     *
     * @param ex      the exception that was thrown
     * @param request the web request during which the exception occurred
     * @return a ResponseEntity containing an ApiResponse with error status, message, and HTTP 500 status code
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleGlobalException(Exception ex, WebRequest request) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles IllegalArgumentException exceptions thrown within the application.
     *
     * @param ex the IllegalArgumentException that was thrown
     * @return a ResponseEntity containing an ApiResponse with error status, message, and HTTP 400 status code
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleIllegalArgument(IllegalArgumentException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles AccountNumberAlreadyExistsException exceptions thrown when attempting
     * to create or register an account with a number that already exists.
     *
     * @param ex the AccountNumberAlreadyExistsException that was thrown
     * @return a ResponseEntity containing an ApiResponse with error status,
     * exception message, and HTTP 409 (Conflict) status code
     */
    @ExceptionHandler(AccountNumberAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleAccountNumberAlreadyExists(AccountNumberAlreadyExistsException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FOUND);
    }

    /**
     * Handles DuplicateAccountException thrown when an account with the same
     * user ID and account type already exists in the system.
     *
     * @param ex the DuplicateAccountException that was thrown
     * @return a ResponseEntity containing an ApiResponse with error status,
     * exception message, and HTTP 409 (Conflict) status code
     */
    @ExceptionHandler(DuplicateAccountException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleDuplicateAccount(DuplicateAccountException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FOUND);
    }

    /**
     * Handles MethodArgumentNotValidException exceptions, which are thrown when validation on a method argument
     * annotated with @Valid fails.
     *
     * @param ex the MethodArgumentNotValidException containing details of the validation failures
     * @return a ResponseEntity containing an ApiResponse with error status, validation error message, and HTTP 400 status code
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(errorMessage);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles AccountNotFoundException thrown when the requested account
     * does not exist in the system.
     *
     * @param ex the AccountNotFoundException that was thrown
     * @return a ResponseEntity containing an ApiResponse with error status,
     * exception message, and HTTP 404 (Not Found) status code
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleAccountNotFound(AccountNotFoundException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles SomethingWentWrongExceptions thrown when an unexpected error
     * occurs during the processing of a request.
     *
     * @param ex the SomethingWentWrongExceptions that was thrown
     * @return a ResponseEntity containing an ApiResponse with error status,
     * exception message, and HTTP 500 (Internal Server Error) status code
     */
    @ExceptionHandler(SomethingWentWrongExceptions.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleAccountNotFound(SomethingWentWrongExceptions ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles InvalidInputException thrown when the provided input values
     * are invalid, malformed, or do not meet validation criteria.
     *
     * @param ex the InvalidInputException that was thrown
     * @return a ResponseEntity containing an ApiResponse with error status,
     * exception message, and HTTP 400 (Bad Request) status code
     */
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleInvalidInputValues(InvalidInputException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles exceptions of type {@link FailedToPublishEventException}.
     *
     * @param ex the {@link FailedToPublishEventException} instance containing the error details
     * @return a {@link ResponseEntity} with an error {@link ApiResponse} and HTTP 500 status
     */
    @ExceptionHandler(FailedToPublishEventException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleFailedToPublishEventException(FailedToPublishEventException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles {@link AccountRollbackFailedException} thrown when an account rollback operation fails.
     *
     * @param ex the {@link AccountRollbackFailedException} instance containing error details
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with the error message
     */
    @ExceptionHandler(AccountRollbackFailedException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleAccountRollbackFailedException(AccountRollbackFailedException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
