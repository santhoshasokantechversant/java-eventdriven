/**
 * @file ICustomerService.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 27,2025
 * @version 1.0
 * @description Service interface for handling operations related to customer entity
 */

package com.techversant.customer_service.service;

import com.google.gson.JsonObject;
import com.techversant.common_lib.events.CustomerReturnDto;
import com.techversant.customer_service.dto.*;
import com.techversant.customer_service.model.Customer;

import java.util.List;
import java.util.UUID;

public interface ICustomerService {

    /**
     * Retrieves a paginated list of customers filtered and sorted according to the provided criteria.
     *
     * @param filterCustomerRequestDto the filter, pagination, and sorting parameters
     * @return a {@link PaginatedCustomerResponseDto} containing the filtered customers and pagination details
     */
    PaginatedCustomerResponseDto viewAllCustomers(FilterCustomerRequestDto filterCustomerRequestDto);

    /**
     * Retrieves an active {@link Customer} by its unique identifier.
     *
     * @param id the unique {@link UUID} of the customer
     * @return the active {@link Customer} with the specified ID, or {@code null} if not found
     */
    Customer getCustomerById(UUID id);

    /**
     * Retrieves a Customer entity associated with the given user ID.
     *
     * @param id the UUID of the user whose customer record is to be fetched
     * @return the Customer entity corresponding to the provided user ID
     */
    Customer getCustomerByUserId(UUID id);

    /**
     * Deletes a customer associated with the given user ID.
     * This method performs necessary validations, checks the status of the customer's
     * account via the Accounts Service, and deletes the customer if all conditions
     * are met. It returns an ApiResponse containing the deleted customer details.
     *
     * @param id the UUID of the user whose customer record is to be deleted
     * @return an ApiResponse containing CustomerReturnDto with the details of the deleted customer
     */
    ApiResponse<CustomerReturnDto> deleteCustomerByUserId(UUID id);


    /**
     * Marks the customer with the given ID as deleted by setting their status to {@code INACTIVE}.
     *
     * @param id the {@link UUID} of the customer to delete
     * @return the {@link Customer} entity after being marked as deleted
     */
    ApiResponse<JsonObject> deleteCustomerById(UUID id);

    /**
     * Creates a new customer with the provided customer details.
     * This method performs the following steps:
     * 1. Validates the customer data with the User Service.
     * 2. Creates a new Customer entity and saves it in the repository.
     * 3. Publishes a CustomerCreatedEvent with a correlation ID for saga tracking.
     * 4. Waits for downstream services (e.g., User and Account) to complete the saga.
     * 5. Rolls back the creation if any step in the saga fails or times out.
     *
     * @param customerDto the DTO containing customer information for creation
     * @return a CustomerResponseDTO containing the details of the newly created customer
     * @throws RuntimeException if validation fails or the saga does not complete successfully
     */
    CustomerResponseDTO createCustomer(CustomerDto customerDto);

    /**
     * Updates the userId field of a customer asynchronously.
     * This method retrieves the customer and corresponding user information,
     * updates the customer's userId, and sends a notification email with
     * the updated account details. The operation is performed asynchronously.
     *
     * @param token      the JWT token used for authorization with external services
     * @param customerId the UUID of the customer whose userId needs to be updated
     * @throws RuntimeException if the account does not exist or email sending fails
     */
    void updateUserId(String token, UUID customerId);

    /**
     * Updates an existing customer's details.
     * This method updates the fields of a customer identified by the given customer number
     * with the values provided in the CustomerDto. It also publishes a CustomerUpdatedEvent
     * to notify other services of the changes. If event publishing fails, the update is
     * persisted but an exception is thrown.
     *
     * @param customerNo  the unique number identifying the customer to update
     * @param customerDTO the DTO containing updated customer information
     * @return a CustomerResponseDTO containing the updated customer details
     * @throws RuntimeException if the customer is not found or event publishing fails
     */
    CustomerResponseDTO updateCustomer(Long customerNo, CustomerDto customerDTO);

    /**
     * Rolls back the customer data for the specified customer number.
     * This method deletes the customer record from the database and publishes
     * a rollback event for auditing purposes. It is typically used when
     * a multistep saga or transaction fails and requires compensation.
     *
     * @param customerNo the unique number identifying the customer to roll back
     * @param reason     the reason for rolling back the customer data
     * @throws RuntimeException if the rollback operation fails
     */
    void rollbackCustomer(Long customerNo, String reason);

    /**
     * Retrieves a list of all active customers.
     *
     * @return a list of Customer entities with Status.ACTIVE
     */
    List<Customer> getCustomerCount();

    /**
     * Deletes a customer and their associated account by email.
     * This method first retrieves the customer using the provided email,
     * deletes the associated account via the Accounts Service, and then
     * removes the customer record from the repository.
     *
     * @param email the email of the customer to be deleted
     */
    void deleteByEmail(String email);
}