/**
 * @file CustomerController.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 27,2025
 * @version 1.0
 * @description Controller to handle all apis related to customers
 */

package com.techversant.customer_service.controller;

import com.google.gson.JsonObject;
import com.techversant.common_lib.events.CustomerReturnDto;
import com.techversant.common_lib.utility.AuditLogger;
import com.techversant.customer_service.dto.*;
import com.techversant.customer_service.model.Customer;
import com.techversant.customer_service.service.ICustomerService;
import com.techversant.customer_service.service.UserIdentityService;
import com.techversant.customer_service.utils.exceptions.CustomerNotFoundException;
import com.techversant.customer_service.utils.exceptions.SomethingWentWrongException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.techversant.customer_service.utils.Constants.*;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final ICustomerService iCustomerService;
    private final UserIdentityService userIdentityService;

    public CustomerController(ICustomerService iCustomerService, UserIdentityService userIdentityService) {
        this.iCustomerService = iCustomerService;
        this.userIdentityService = userIdentityService;
    }

    /**
     * REST endpoint to retrieve a paginated list of all customers based on filter criteria.
     * Accepts query parameters mapped to FilterCustomerRequestDto and returns a paginated
     * response containing customer details.
     * Workflow:
     * 1. Validates the incoming filter request using @Valid.
     * 2. Calls ICustomerService.viewAllCustomers(FilterCustomerRequestDto) to fetch filtered customers.
     * 3. Wraps the paginated customer response in an ApiResponse object.
     * 4. Sets the status and message indicating success.
     * 5. Returns the response with HTTP 200 (OK).
     *
     * @param filterCustomerRequestDto the filter criteria for retrieving customers
     * @return a ResponseEntity containing an ApiResponse with paginated customer data
     */
    @GetMapping(path = "/view-all-customers")
    public ResponseEntity<ApiResponse<PaginatedCustomerResponseDto>> viewAllCustomers(@ModelAttribute @Valid FilterCustomerRequestDto filterCustomerRequestDto) {
        PaginatedCustomerResponseDto paginatedCustomerResponseDto = iCustomerService.viewAllCustomers(filterCustomerRequestDto);
        ApiResponse<PaginatedCustomerResponseDto> apiResponse = new ApiResponse<>();
        apiResponse.setData(paginatedCustomerResponseDto);
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(CUSTOMERS_RETRIEVED);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * REST endpoint to retrieve a customer by their unique ID.
     * Fetches the customer from the service layer. If the customer is not found,
     * throws CustomerNotFoundException.
     *
     * @param id the UUID of the customer to retrieve
     * @return a ResponseEntity containing an ApiResponse with the customer data
     * @throws CustomerNotFoundException if no customer is found with the given ID
     */
    @GetMapping(path = "/get-customer-by-id/{id}")
    public ResponseEntity<ApiResponse<Customer>> getCustomerById(@PathVariable UUID id) {
        Customer customer = iCustomerService.getCustomerById(id);
        if (customer == null) {
            throw new CustomerNotFoundException(CUSTOMER_NOT_FOUND);
        }
        ApiResponse<Customer> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(CUSTOMER_RETRIEVED);
        apiResponse.setData(customer);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * REST endpoint to retrieve a customer by the associated user ID.
     * Fetches the customer from the service layer using the user ID. If no customer
     * is found, throws CustomerNotFoundException.
     *
     * @param id the UUID of the user associated with the customer
     * @return a ResponseEntity containing an ApiResponse with the customer data
     * @throws CustomerNotFoundException if no customer is found for the given user ID
     */
    @GetMapping(path = "/get-customer-by-user-id/{id}")
    public ResponseEntity<ApiResponse<Customer>> getCustomerByUserId(@PathVariable UUID id) {
        Customer customer = iCustomerService.getCustomerByUserId(id);
        if (customer == null) {
            throw new CustomerNotFoundException(CUSTOMER_NOT_FOUND);
        }
        ApiResponse<Customer> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(CUSTOMER_RETRIEVED);
        apiResponse.setData(customer);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * REST endpoint to delete a customer by the associated user ID.
     * Calls the service layer to delete the customer and returns a response
     * containing details of the deleted customer.
     *
     * @param id the UUID of the user associated with the customer to delete
     * @return a ResponseEntity containing an ApiResponse with the deleted customer details
     */
    @GetMapping(path = "/delete-customer-by-user-id/{id}")
    public ResponseEntity<ApiResponse<CustomerReturnDto>> deleteCustomerByUserId(@PathVariable UUID id) {
        ApiResponse<CustomerReturnDto> customerResponse = iCustomerService.deleteCustomerByUserId(id);
        return ResponseEntity.ok(customerResponse);
    }

    /**
     * Deletes a customer by their unique ID.
     * This endpoint performs the following steps:
     * 1. Retrieves the current username and client IP from the request.
     * 2. Calls the service layer to delete the customer.
     * 3. Logs the deletion using AuditLogger and structured logs for monitoring.
     * 4. Handles exceptions by logging the error and returning an appropriate error response.
     *
     * @param id the UUID of the customer to delete
     * @param request the HTTP request containing user and client information
     * @return a ResponseEntity containing an ApiResponse with deletion status or error details
     */
    @DeleteMapping(path = "/delete-customer-by-id/{id}")
    public ResponseEntity<ApiResponse<JsonObject>> deleteCustomerById(
            @PathVariable UUID id,
            HttpServletRequest request) {

        String currentUser = userIdentityService.getCurrentUsername(request);
        String clientIp = request.getRemoteAddr();

        final String userId = currentUser;
        final String userIp = clientIp;

        try {
            ApiResponse<JsonObject> response = iCustomerService.deleteCustomerById(id);

            AuditLogger.log(
                    userId,                                   // user
                    userIp,                                   // ip
                    CUSTOMER,                               // entity
                    "DELETE_CUSTOMER: " + id,                 // action
                    SUCCESS                                 // status
            );

            //  Keep existing structured log for ELK
            log.info("Customer deleted: {}", Map.of(
                    EVENT_TYPE, "CUSTOMER_DELETED",
                    USER_ID, userId,
                    CUSTOMER_ID, id.toString(),
                    ACTION, "DELETE",
                    TIMESTAMP, Instant.now().toString(),
                    STATUS, SUCCESS
            ));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // AUDIT LOG: Customer deletion failed
            AuditLogger.log(
                    userId,                                   // user
                    userIp,                                   // ip
                    CUSTOMER,                               // entity
                    "DELETE_CUSTOMER: " + id + " - Error: " + e.getMessage(), // action
                    FAILED                                  // status
            );

            log.error("Customer deletion failed: {}", Map.of(
                    EVENT_TYPE, "CUSTOMER_DELETE_FAILED",
                    USER_ID, userId,
                    CUSTOMER_ID, id.toString(),
                    ACTION, "DELETE",
                    TIMESTAMP, Instant.now().toString(),
                    STATUS, FAILED,
                    ERROR_MESSAGE, e.getMessage()
            ));

            // Re-throw or return a specific error response
            ApiResponse<JsonObject> errorResponse = new ApiResponse<>();
            errorResponse.setStatus(STATUS_ERROR);
            errorResponse.setMessage("Customer deletion failed: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates an existing customer based on the provided customer number and update details.
     *
     * Steps:
     * 1. Retrieves the current username and client IP from the request.
     * 2. Calls the service layer to update the customer with the provided CustomerDto.
     * 3. Logs the fields that were updated for auditing purposes using AuditLogger.
     * 4. Maintains structured logs for monitoring with updated field information.
     * 5. Handles exceptions by logging the error and returning an error response.
     *
     * @param customerNo the unique number of the customer to update
     * @param customerDTO the data transfer object containing fields to update
     * @param request the HTTP request containing user and client information
     * @return ResponseEntity containing an ApiResponse with the updated customer data
     */
    @PutMapping("/{customerNo}")
    @CircuitBreaker(name = "customerController", fallbackMethod = "updateCustomerFallback")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> updateCustomer(
            @PathVariable Long customerNo,
            @Valid @RequestBody CustomerDto customerDTO,
            HttpServletRequest request) {

        String currentUser = userIdentityService.getCurrentUsername(request);
        String clientIp = request.getRemoteAddr();

        final String userId = currentUser;
        final String userIp = clientIp;

        try {
            CustomerResponseDTO updatedCustomer = iCustomerService.updateCustomer(customerNo, customerDTO);

            ApiResponse<CustomerResponseDTO> apiResponse = new ApiResponse<>();
            apiResponse.setStatus(STATUS_SUCCESS);
            apiResponse.setMessage(CUSTOMER_UPDATED_SUCCESSFULLY);
            apiResponse.setData(updatedCustomer);

            // CAPTURE FIELD UPDATES: Log all fields that were sent in the update request
            StringBuilder fieldChanges = new StringBuilder();

            if (customerDTO.getEmail() != null) {
                fieldChanges.append("email, ");
            }
            if (customerDTO.getFirstName() != null) {
                fieldChanges.append("firstName, ");
            }
            if (customerDTO.getLastName() != null) {
                fieldChanges.append("lastName, ");
            }
            if (customerDTO.getPhoneNumber() != null) {
                fieldChanges.append("phoneNumber, ");
            }
            if (customerDTO.getStatus() != null) {
                fieldChanges.append("status, ");
            }
            if (customerDTO.getAddress() != null) {
                fieldChanges.append("address, ");
            }
            if (customerDTO.getCity() != null) {
                fieldChanges.append("city, ");
            }
            if (customerDTO.getState() != null) {
                fieldChanges.append("state, ");
            }
            if (customerDTO.getCountry() != null) {
                fieldChanges.append("country, ");
            }
            if (customerDTO.getPostalCode() != null) {
                fieldChanges.append("postalCode, ");
            }

            String updatedFields = fieldChanges.toString();
            if (updatedFields.endsWith(", ")) {
                updatedFields = updatedFields.substring(0, updatedFields.length() - 2);
            }

            // AUDIT LOG with field updates
            if (!updatedFields.isEmpty()) {
                AuditLogger.log(
                        userId,                                   // user
                        userIp,                                   // ip
                        CUSTOMER,                               // entity
                        UPDATE_CUSTOMER + customerNo + " - Updated fields: " + updatedFields, // action with field changes
                        SUCCESS                                  // status
                );
            } else {
                AuditLogger.log(
                        userId,                                   // user
                        userIp,                                   // ip
                        CUSTOMER,                               // entity
                        UPDATE_CUSTOMER + customerNo + " - No fields updated", // action
                        SUCCESS                                 // status
                );
            }

            // Keep existing structured log for ELK with field info
            log.info("Customer updated: {}", Map.of(
                    EVENT_TYPE, "CUSTOMER_UPDATED",
                    USER_ID, userId,
                    "customerNo", customerNo,
                    ACTION, "UPDATE",
                    TIMESTAMP, Instant.now().toString(),
                    STATUS, SUCCESS,
                    "updatedFields", updatedFields.isEmpty() ? "none" : updatedFields
            ));

            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            // AUDIT LOG: Customer update failed
            AuditLogger.log(
                    userId,                                   // user
                    userIp,                                   // ip
                    CUSTOMER,                               // entity
                    UPDATE_CUSTOMER + customerNo + " - Error: " + e.getMessage(), // action
                    FAILED                                 // status
            );

            log.error("Customer update failed: {}", Map.of(
                    EVENT_TYPE, "CUSTOMER_UPDATE_FAILED",
                    USER_ID, userId,
                    "customerNo", customerNo,
                    ACTION, "UPDATE",
                    TIMESTAMP, Instant.now().toString(),
                    STATUS, FAILED,
                    ERROR_MESSAGE, e.getMessage()
            ));

            ApiResponse<CustomerResponseDTO> apiResponse = new ApiResponse<>();
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("Customer update failed: " + e.getMessage());
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Creates a new customer using the provided customer details.
     *
     * Steps:
     * 1. Retrieves the current username and client IP from the HTTP request.
     * 2. Calls the service layer to create a new customer with the given CustomerDto.
     * 3. If the customer is created successfully:
     *    - Updates the associated user ID using the authorization token.
     *    - Logs the creation event using AuditLogger for auditing purposes.
     *    - Writes structured log entries for monitoring and ELK.
     * 4. If creation fails (service returns null):
     *    - Logs failure using AuditLogger.
     *    - Writes an error log entry with reason.
     * 5. Handles exceptions by logging the error, writing audit logs, and returning an error response.
     *
     * @param customerDTO the data transfer object containing new customer information
     * @param request the HTTP request containing user and client information
     * @return ResponseEntity containing an ApiResponse with the created customer data or an error message
     */
    @PostMapping("/create-customer")
    @CircuitBreaker(name = "customerController", fallbackMethod = "createCustomerFallback")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> createCustomer(
            @Valid @RequestBody CustomerDto customerDTO,
            HttpServletRequest request) {

        String currentUser = userIdentityService.getCurrentUsername(request);
        String clientIp = getClientIpAddress(request);

        final String userId = currentUser;
        final String userIp = clientIp;

        try {
            CustomerResponseDTO createdCustomer = iCustomerService.createCustomer(customerDTO);
            ApiResponse<CustomerResponseDTO> apiResponse = new ApiResponse<>();

            if (createdCustomer != null) {
                String authHeader = request.getHeader("Authorization");
                String token = null;
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
                iCustomerService.updateUserId(token, createdCustomer.getCustomerId());

                apiResponse.setStatus(STATUS_SUCCESS);
                apiResponse.setMessage(CUSTOMER_CREATED_SUCCESSFULLY);
                apiResponse.setData(createdCustomer);

                // AUDIT LOG using AuditLogger (consistent with gateway pattern)
                AuditLogger.log(
                        userId,                                   // user
                        userIp,                                   // ip
                        CUSTOMER,                               // entity
                        "CREATE_CUSTOMER: " + createdCustomer.getCustomerId(), // action
                        SUCCESS                                 // status
                );

                // Keep existing structured log for ELK (optional)
                log.info("Customer created: {}", Map.of(
                        EVENT_TYPE, "CUSTOMER_CREATED",
                        USER_ID, userId,
                        CUSTOMER_ID, createdCustomer.getCustomerId(),
                        ACTION, CREATE,
                        TIMESTAMP, Instant.now().toString(),
                        STATUS, SUCCESS,
                        "customerEmail", customerDTO.getEmail(),
                        "customerName", customerDTO.getFirstName() + " " + customerDTO.getLastName()
                ));

            } else {
                apiResponse.setStatus(STATUS_ERROR);
                apiResponse.setMessage("Failed to create Customer.");

                // AUDIT LOG for failure using AuditLogger
                AuditLogger.log(
                        userId,                                   // user
                        userIp,                                   // ip
                        CUSTOMER,                               // entity
                        "CREATE_CUSTOMER",                        // action
                        FAILED                                  // status
                );

                log.error("Customer creation failed: {}", Map.of(
                        EVENT_TYPE, "CUSTOMER_CREATE_FAILED",
                        USER_ID, userId,
                        ACTION, CREATE,
                        TIMESTAMP, Instant.now().toString(),
                        STATUS, FAILED,
                        "reason", "Service returned null customer"
                ));
            }
            return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            ApiResponse<CustomerResponseDTO> apiResponse = new ApiResponse<>();
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("Customer creation failed: " + e.getMessage());

            // AUDIT LOG for exception using AuditLogger
            AuditLogger.log(
                    userId,                                   // user
                    userIp,                                   // ip
                    CUSTOMER,                               // entity
                    "CREATE_CUSTOMER - Error: " + e.getMessage(), // action
                    "ERROR"                                   // status
            );

            log.error("Customer creation error: {}", Map.of(
                    EVENT_TYPE, "CUSTOMER_CREATE_ERROR",
                    USER_ID, userId,
                    ACTION, CREATE,
                    TIMESTAMP, Instant.now().toString(),
                    STATUS, "ERROR",
                    ERROR_MESSAGE, e.getMessage(),
                    "customerEmail", customerDTO != null ? customerDTO.getEmail() : "unknown"
            ));

            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves the client's IP address from the HTTP request.
     * If the "X-Forwarded-For" header is present (typically set by proxies or load balancers),
     * it returns the first IP in the comma-separated list. Otherwise, it returns the remote address
     * from the request.
     *
     * @param request the HttpServletRequest object
     * @return the client's IP address as a String
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0]; // First IP in the chain
        }
        return request.getRemoteAddr();
    }

    /**
     * Fallback method for customer creation when the main service is unavailable.
     * This method is invoked by the circuit breaker if the createCustomer endpoint fails.
     * It returns a SERVICE_UNAVAILABLE (503) response with an error message.
     *
     * The signature must match createCustomer plus a trailing Throwable.
     *
     * @param customerDTO the customer data from the original request
     * @param request the original HTTP request
     * @param throwable the exception that triggered the fallback
     * @return ResponseEntity containing an ApiResponse with error status and message
     */
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> createCustomerFallback(
            CustomerDto customerDTO, HttpServletRequest request, Throwable throwable) {

        ApiResponse<CustomerResponseDTO> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_ERROR);
        apiResponse.setMessage("Customer creation service unavailable: " + throwable.getMessage());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(apiResponse);
    }

    /**
     * Fallback method for customer update when the main service is unavailable.
     * This method is invoked by the circuit breaker if the updateCustomer endpoint fails.
     * It returns a SERVICE_UNAVAILABLE (503) response with an error message.
     *
     * The signature must match updateCustomer plus a trailing Throwable.
     *
     * @param customerNo the customer number from the original request
     * @param customerDTO the customer data from the original request
     * @param request the original HTTP request
     * @param t the exception that triggered the fallback
     * @return ResponseEntity containing an ApiResponse with error status and message
     */
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> updateCustomerFallback(
            Long customerNo, CustomerDto customerDTO, HttpServletRequest request, Throwable t) {
        ApiResponse<CustomerResponseDTO> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_ERROR);
        apiResponse.setMessage("Customer update service unavailable: " + t.getMessage());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(apiResponse);
    }

    /**
     * REST endpoint to retrieve the total number of customers.
     * This method calls the customer service to get all customers and returns
     * the total count wrapped in an ApiResponse object. In case of any runtime
     * exception, a SomethingWentWrongException is thrown.
     *
     * @return ResponseEntity containing ApiResponse with the total customer count
     */
    @GetMapping("/users-count")
    public ResponseEntity<ApiResponse<Void>> usersCount() {
        try {
            List<Customer> user = iCustomerService.getCustomerCount();
            int count = user.size();
            ApiResponse<Void> apiResponse = new ApiResponse<>();
            apiResponse.setStatus(STATUS_SUCCESS);
            apiResponse.setMessage("total counts");
            apiResponse.setCount(count);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            throw new SomethingWentWrongException("Failed to fetch Count");
        }
    }

    /**
     * REST endpoint to delete a customer by their email address.
     * This method invokes the customer service to delete the customer associated
     * with the given email. Returns HTTP 204 (No Content) on successful deletion.
     *
     * @param email the email address of the customer to delete
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/email/{email}")
    public ResponseEntity<Void> deleteCustomerByEmail(@PathVariable String email) {
        iCustomerService.deleteByEmail(email);
        return ResponseEntity.noContent().build();
    }

}
