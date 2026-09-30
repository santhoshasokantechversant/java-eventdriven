/**
 * @file GlobalExceptionHandler.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 06,2025
 * @version 1.0
 * @description Global exception handler to handle all exceptions in user-service
 */

package com.techversant.userservice.config;

import com.google.gson.JsonObject;
import com.techversant.userservice.dto.ApiResponse;
import com.techversant.userservice.utils.Constants;
import com.techversant.userservice.utils.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;

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
     * Handles UserNotFoundException exceptions thrown when a requested user is not found in the system.
     *
     * @param ex the UserNotFoundException that was thrown
     * @return a ResponseEntity containing an ApiResponse with error status, message, and HTTP 404 status code
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleUserNotFound(UserNotFoundException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles EmailAlreadyExistsException exceptions thrown when attempting to register or update a user
     * with an email address that is already in use.
     *
     * @param ex the EmailAlreadyExistsException that was thrown
     * @return a ResponseEntity containing an ApiResponse with error status, message, and HTTP 302 status code
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FOUND);
    }

    /**
     * Handles UserNameAlreadyExistsException exceptions thrown when attempting to register or update a user
     * with an already existing username.
     *
     * @param ex the UserNameAlreadyExistsException that was thrown
     * @return a ResponseEntity containing an ApiResponse with error status, message, and HTTP 409 status code
     */
    @ExceptionHandler(UserNameAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleUserNameAlreadyExists(UserNameAlreadyExistsException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FOUND);
    }

    /**
     * Exception handler for SomethingWentWrongException.
     *
     * @param ex the exception thrown
     * @return a ResponseEntity containing the error response and HTTP status
     */

    @ExceptionHandler(SomethingWentWrongException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleSomethingWentWrong(SomethingWentWrongException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FOUND);
    }


    /**
     * Exception handler for RoleNameAlreadyExistException.
     *
     * @param ex the RoleNameAlreadyExistException thrown when attempting to create a duplicate role
     * @return ResponseEntity containing the API error response and HTTP status
     */
    @ExceptionHandler(RoleNameAlreadyExistException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleSomethingWentWrong(RoleNameAlreadyExistException ex) {
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
     * Handles MethodArgumentTypeMismatchException exceptions thrown when a method argument
     * fails to convert to the expected type, typically due to invalid parameter values in the request.
     *
     * @param ex the MethodArgumentTypeMismatchException containing details about the type mismatch
     * @return a ResponseEntity containing an ApiResponse with error status, detailed error message, and HTTP 400 status code
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleArgumentTypeMismatchExceptions(MethodArgumentTypeMismatchException ex) {
        Class<?> requiredType = ex.getRequiredType();
        String errorMessage = String.format(
                "Invalid value '%s' for parameter '%s'. Expected a value of type '%s'.",
                ex.getValue(),
                ex.getName(),
                requiredType
        );
        if (requiredType != null && requiredType.isEnum()) {
            Class<?> enumType = ex.getRequiredType();
            if (enumType != null && enumType.isEnum()) {
                Object[] enumConstants = enumType.getEnumConstants();
                if (enumConstants != null) {
                    String[] acceptedValues = Arrays.stream(enumType.getEnumConstants())
                            .map(Object::toString)
                            .toArray(String[]::new);
                    errorMessage = String.format(
                            "Invalid value '%s' for parameter '%s'. Allowed values are: %s.",
                            ex.getValue(),
                            ex.getName(),
                            String.join(", ", acceptedValues)
                    );
                } else {
                    errorMessage = String.format(
                            "Invalid value '%s' for parameter '%s'.",
                            ex.getValue(),
                            ex.getName()
                    );
                }
            }

        }
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(errorMessage);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles UpdateUserFailedException, which is thrown when a user's update operation fails.
     *
     * @param ex the UpdateUserFailedException containing the error message explaining the cause of failure
     * @return a ResponseEntity containing an ApiResponse with error status, the exception message,
     * and HTTP 500 (Internal Server Error) status code
     */
    @ExceptionHandler(UpdateUserFailedException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleUpdateUserFailedException(UpdateUserFailedException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles DeleteUserFailedException which is thrown when a user deletion operation fails.
     *
     * @param ex the DeleteUserFailedException containing the error message explaining the failure
     * @return a ResponseEntity containing an ApiResponse with error status,
     * the exception message, and HTTP 500 (Internal Server Error) status code
     */
    @ExceptionHandler(DeleteUserFailedException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleDeleteUserFailedException(DeleteUserFailedException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Exception handler for RoleNotFoundException.
     *
     * @param ex the RoleNotFoundException thrown when a role is not found
     * @return ResponseEntity containing ApiResponse with error status and message
     */
    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleRoleNotFoundException(RoleNotFoundException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PhoneNumberAlreadyExistException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handlePhoneNumberAlreadyExistException(PhoneNumberAlreadyExistException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Exception handler for DeleteRoleFailedException.
     *
     * @param ex the exception thrown when role deletion fails
     * @return ResponseEntity containing ApiResponse with error status and message
     */
    @ExceptionHandler(DeleteRoleFailedException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleDeleteRoleFailedException(DeleteRoleFailedException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Exception handler for UpdateRoleFailedException.
     *
     * @param ex the exception thrown when a role update operation fails
     * @return ResponseEntity containing ApiResponse with error status and failure message
     */
    @ExceptionHandler(UpdateRoleFailedException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleUpdateRoleFailedException(UpdateRoleFailedException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Exception handler for FailedToAssignRoleToUser exception.
     *
     * @param ex the exception thrown when assigning a role to a user fails
     * @return ResponseEntity containing ApiResponse with error status and failure message
     */
    @ExceptionHandler(FailedToAssignRoleToUser.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleFailedToAssignRoleToUserException(FailedToAssignRoleToUser ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Exception handler for InvalidUuidException.
     *
     * @param ex the exception thrown when an invalid UUID is encountered
     * @return ResponseEntity containing ApiResponse with error status and failure message
     */
    @ExceptionHandler(InvalidUuidException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleInvalidUuid(InvalidUuidException ex) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(Constants.STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
