/**
 * @file GlobalExceptionHandler.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @version 1.0
 * @description Handles exceptions globally in the application
 */

package com.techversant.customer_service.handler;

import com.google.gson.JsonObject;
import com.techversant.customer_service.dto.ApiResponse;
import com.techversant.customer_service.utils.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

import static com.techversant.customer_service.utils.Constants.INVALID_REQUEST_PARAMETERS;
import static com.techversant.customer_service.utils.Constants.STATUS_ERROR;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Global exception handler for RuntimeException in the application.
     * Logs the exception and returns an appropriate HTTP response:
     * - If the exception message contains "Service unavailable", returns HTTP 503 (Service Unavailable)
     * with a generic temporary service unavailable message.
     * - Otherwise, returns HTTP 500 (Internal Server Error) with the exception message.
     *
     * @param ex the RuntimeException that was thrown
     * @return a ResponseEntity containing the error message and HTTP status
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleServiceUnavailable(RuntimeException ex) {
        logger.error("Service error: {}", ex.getMessage());

        if (ex.getMessage().contains("Service unavailable")) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Service temporarily unavailable. Please try again later.");
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred: " + ex.getMessage());
    }

    /**
     * Handles all uncaught exceptions thrown within the application.
     * Returns a {@code ResponseEntity} with a custom {@code ApiResponse} containing the exception message and a {@code 500 INTERNAL_SERVER_ERROR} HTTP status code.
     *
     * @param ex      the {@link Exception} that was thrown during request processing
     * @param request the {@link WebRequest} during which the exception occurred
     * @return a {@link ResponseEntity} containing an {@code ApiResponse} with error status, message and HTTP 500 status code
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleGlobalException(Exception ex, WebRequest request) {
        ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
        errorResponse.setStatus(STATUS_ERROR);
        errorResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles validation errors that occur during request binding (e.g., invalid enum values, wrong data types).
     * Returns a {@code ResponseEntity} with a custom {@code ApiResponse} containing the error message and a
     * {@code 400 BAD_REQUEST} HTTP status code.
     *
     * @param ex the {@link BindException} thrown when request parameters fail validation
     * @return a {@link ResponseEntity} containing an {@code ApiResponse} with error status, message and HTTP 400 status code
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleBindException(BindException ex) {
        String errorMessage = INVALID_REQUEST_PARAMETERS;
        if (!ex.getBindingResult().getFieldErrors().isEmpty()) {
            FieldError fieldError = ex.getBindingResult().getFieldErrors().get(0);
            String fieldName = fieldError.getField();
            Object rejectedValue = fieldError.getRejectedValue();
            Class<?> requiredType = Objects.requireNonNull(ex.getTarget()).getClass();
            try {
                Field field = requiredType.getDeclaredField(fieldName);
                Class<?> fieldType = field.getType();
                if (fieldType.isEnum()) {
                    String[] allowedValues = Arrays.stream(fieldType.getEnumConstants())
                            .map(Object::toString)
                            .toArray(String[]::new);
                    errorMessage = String.format(
                            "Invalid value '%s' for field '%s'. Allowed values are: %s.",
                            rejectedValue,
                            fieldName,
                            String.join(", ", allowedValues)
                    );
                } else {
                    errorMessage = String.format(
                            "Invalid value '%s' for field '%s'.",
                            rejectedValue,
                            fieldName
                    );
                }
            } catch (NoSuchFieldException noSuchFieldException) {
                errorMessage = String.format(
                        "Invalid value '%s' for field '%s'.",
                        rejectedValue,
                        fieldName
                );
            }
        }
        ApiResponse<JsonObject> apiResponse = new ApiResponse<>();
        apiResponse.setStatus("ERROR");
        apiResponse.setMessage(errorMessage);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link FailedToFilterCustomersException} thrown during customer filtering operations.
     * Returns a {@code ResponseEntity} with a custom {@code ApiResponse} containing the exception message and a {@code 500 INTERNAL_SERVER_ERROR} HTTP status code.
     *
     * @param ex the {@link FailedToFilterCustomersException} that was thrown
     * @return a {@link ResponseEntity} containing an {@code ApiResponse} with error status, message and HTTP 500 status code
     */
    @ExceptionHandler(FailedToFilterCustomersException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleFailedToFilterCustomersException(FailedToFilterCustomersException ex) {
        ApiResponse<JsonObject> apiResponse = new ApiResponse<>();
        apiResponse.setMessage(ex.getMessage());
        apiResponse.setStatus(STATUS_ERROR);
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles {@link CustomerNotFoundException} thrown when a customer is not found in the system.
     * Returns a {@code ResponseEntity} with a custom {@code ApiResponse} containing the error message and a
     * {@code 404 NOT_FOUND} HTTP status code.
     *
     * @param ex the {@link CustomerNotFoundException} that was thrown
     * @return a {@link ResponseEntity} containing an {@code ApiResponse} with error status, message and HTTP 404 status code
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleCustomerNotFoundException(CustomerNotFoundException ex) {
        ApiResponse<JsonObject> apiResponse = new ApiResponse<>();
        apiResponse.setMessage(ex.getMessage());
        apiResponse.setStatus(STATUS_ERROR);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link MethodArgumentTypeMismatchException} thrown when a request parameter cannot be converted to the expected type.
     * Returns a {@code ResponseEntity} with a custom {@code ApiResponse} containing a detailed error message and a {@code 400 BAD_REQUEST} HTTP status code.
     *
     * @param ex the {@link MethodArgumentTypeMismatchException} that was thrown
     * @return a {@link ResponseEntity} containing an {@code ApiResponse} with error status, message and HTTP 400 status code
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
        errorResponse.setStatus(STATUS_ERROR);
        errorResponse.setMessage(errorMessage);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link FailedToDeleteCustomerException} thrown when an error occurs while attempting to delete a customer.
     * Returns a {@code ResponseEntity} with a custom {@code ApiResponse} containing the error message and a {@code 500 INTERNAL_SERVER_ERROR} HTTP status code.
     *
     * @param ex the {@link FailedToDeleteCustomerException} that was thrown
     * @return a {@link ResponseEntity} containing an {@code ApiResponse} with error status, message, and HTTP 500 status code
     */
    @ExceptionHandler(FailedToDeleteCustomerException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleFailedToDeleteCustomerException(FailedToDeleteCustomerException ex) {
        ApiResponse<JsonObject> apiResponse = new ApiResponse<>();
        apiResponse.setMessage(ex.getMessage());
        apiResponse.setStatus(STATUS_ERROR);
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles {@link RegionNotFoundException} thrown when a specified region is not found.
     * Returns a {@code ResponseEntity} with a custom {@code ApiResponse} containing the error message and a {@code 404 Not Found} HTTP status code.
     *
     * @param ex the {@link RegionNotFoundException} containing details about the missing region
     * @return a {@link ResponseEntity} containing an {@code ApiResponse} with an error message and HTTP 404 status code
     */
    @ExceptionHandler(RegionNotFoundException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleRegionNotFoundException(RegionNotFoundException ex) {
        ApiResponse<JsonObject> apiResponse = new ApiResponse<>();
        apiResponse.setMessage(ex.getMessage());
        apiResponse.setStatus(STATUS_ERROR);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link CountryNotFoundException} thrown when a specified country is not found.
     * Returns a {@code ResponseEntity} with a custom {@code ApiResponse} containing the error message and a {@code 404 Not Found} HTTP status code.
     *
     * @param ex the {@link CountryNotFoundException} containing details about the missing country
     * @return a {@link ResponseEntity} containing an {@code ApiResponse} with an error message and HTTP 404 status code
     */
    @ExceptionHandler(CountryNotFoundException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleCountryNotFoundException(CountryNotFoundException ex) {
        ApiResponse<JsonObject> apiResponse = new ApiResponse<>();
        apiResponse.setMessage(ex.getMessage());
        apiResponse.setStatus(STATUS_ERROR);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link StateNotFoundException} thrown when a specified state is not found.
     * Returns a {@code ResponseEntity} with a custom {@code ApiResponse} containing the error message and a {@code 404 Not Found} HTTP status code.
     *
     * @param ex the {@link StateNotFoundException} containing details about the missing state
     * @return a {@link ResponseEntity} containing an {@code ApiResponse} with an error message and HTTP 404 status code
     */
    @ExceptionHandler(StateNotFoundException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleStateNotFoundException(StateNotFoundException ex) {
        ApiResponse<JsonObject> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_ERROR);
        apiResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Exception handler for SomethingWentWrongException.
     * Converts the exception into an ApiResponse with an error status and the exception message,
     * and returns it with HTTP 404 (Not Found) status.
     *
     * @param ex the SomethingWentWrongException that was thrown
     * @return a ResponseEntity containing the ApiResponse with error status and message
     */
    @ExceptionHandler(SomethingWentWrongException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleSomethingWentWrongException(SomethingWentWrongException ex) {
        ApiResponse<JsonObject> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_ERROR);
        apiResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link FailedToCreateCustomerException} that occurs when creating a customer fails.
     * This method catches the exception and constructs a standardized API response
     * with an error status and descriptive message. The HTTP status is set to 500
     * (Internal Server Error) to indicate that the issue originated from the server.
     *
     * @param ex the {@link FailedToCreateCustomerException} containing details about the failure
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with the error information
     */
    @ExceptionHandler(FailedToCreateCustomerException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleFailedToCreateCustomerException(FailedToCreateCustomerException ex) {
        ApiResponse<JsonObject> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_ERROR);
        apiResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles {@link FailedToPublishEventException} that occurs when publishing an event fails.
     * This method catches the exception and constructs a standardized API response
     * with an error status and descriptive message. The HTTP status is set to 500
     * (Internal Server Error) to indicate a server-side issue during event publishing.
     *
     * @param ex the {@link FailedToPublishEventException} containing details about the failure
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with the error information
     */
    @ExceptionHandler(FailedToPublishEventException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleFailedToPublishEventException(FailedToPublishEventException ex) {
        ApiResponse<JsonObject> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_ERROR);
        apiResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles {@link FailedToSendEmailException} that occurs when sending an email fails.
     * This method constructs a standardized API response containing an error status
     * and the exception message. It returns an HTTP 500 (Internal Server Error) response
     * to indicate a server-side issue during email sending.
     *
     * @param ex the {@link FailedToSendEmailException} containing details about the failure
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with the error information
     */
    @ExceptionHandler(FailedToSendEmailException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleFailedToSendEmailException(FailedToSendEmailException ex) {
        ApiResponse<JsonObject> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_ERROR);
        apiResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles {@link FailedToUpdateCustomerException} that occurs when updating
     * a customer fails.
     * This method constructs a standardized API response containing an error status
     * and the exception message. It returns an HTTP 500 (Internal Server Error) response
     * to indicate a server-side failure during the customer update process.
     *
     * @param ex the {@link FailedToUpdateCustomerException} containing details about the failure
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with the error information
     */
    @ExceptionHandler(FailedToUpdateCustomerException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleFailedToUpdateCustomerException(FailedToUpdateCustomerException ex) {
        ApiResponse<JsonObject> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_ERROR);
        apiResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles {@link CustomerRollbackFailedException} that occurs when a customer rollback
     * operation fails.
     * This method constructs a standardized API response containing an error status
     * and the exception message. It returns an HTTP 500 (Internal Server Error) response
     * to indicate a server-side failure during the rollback process.
     *
     * @param ex the {@link CustomerRollbackFailedException} containing details about the failure
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with the error information
     */
    @ExceptionHandler(CustomerRollbackFailedException.class)
    public ResponseEntity<ApiResponse<JsonObject>> handleCustomerRollbackFailedException(CustomerRollbackFailedException ex) {
        ApiResponse<JsonObject> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_ERROR);
        apiResponse.setMessage(ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
