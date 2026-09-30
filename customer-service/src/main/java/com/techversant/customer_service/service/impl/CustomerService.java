/**
 * @file CustomerService.java
 * @company Techversant Infotech
 * @author Siya Elsa Sabu
 * @version 1.0
 * @description Class implementing methods on ICustomerService interface
 */

package com.techversant.customer_service.service.impl;

import com.google.gson.JsonObject;
import com.techversant.common_lib.events.*;
import com.techversant.customer_service.config.EmailService;
import com.techversant.customer_service.dto.*;
import com.techversant.customer_service.mapper.CustomerMapper;
import com.techversant.customer_service.model.Customer;
import com.techversant.customer_service.model.CustomerUserEntity;
import com.techversant.customer_service.repository.CustomerRepository;
import com.techversant.customer_service.repository.CustomerUserRepository;
import com.techversant.customer_service.service.CustomerCreationSagaTracker;
import com.techversant.customer_service.service.ICustomerService;
import com.techversant.customer_service.service.feignclient.AccountsServiceClient;
import com.techversant.customer_service.service.webclient.AccountsWebclientService;
import com.techversant.customer_service.service.webclient.UserWebClientService;
import com.techversant.customer_service.utils.UtilityMethods;
import com.techversant.customer_service.utils.enums.Status;
import com.techversant.customer_service.utils.exceptions.*;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.query.SortDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static com.techversant.customer_service.utils.Constants.*;

@Service
public class CustomerService implements ICustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
    private static final String CUSTOMER_CREATED_TOPIC = "customer.created";
    private static final String CUSTOMER_UPDATED_TOPIC = "customer.updated";
    private static final String CUSTOMER_ROLLBACK_TOPIC = "customer.rollback";

    @Value("${service.register-url}")
    private String registerFrontendUrl;

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final HttpServletRequest request;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserWebClientService userWebClientService;
    private final EntityManager entityManager;
    private final AccountsServiceClient accountsServiceClient;
    private final CustomerCreationSagaTracker sagaTracker;
    private final CustomerUserRepository customerUserRepository;
    private final AccountsWebclientService accountsWebclientService;

    public CustomerService(CustomerUserRepository customerUserRepository, EmailService emailService,
                           TemplateEngine templateEngine,
                           AccountsWebclientService accountsWebclientService,
                           AccountsServiceClient accountsServiceClient,
                           CustomerRepository customerRepository, CustomerMapper customerMapper,
                           UserWebClientService userWebClientService, EntityManager entityManager,
                           HttpServletRequest request,
                           KafkaTemplate<String, Object> kafkaTemplate,
                           CustomerCreationSagaTracker sagaTracker
    ) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.userWebClientService = userWebClientService;
        this.entityManager = entityManager;
        this.accountsServiceClient = accountsServiceClient;
        this.accountsWebclientService = accountsWebclientService;
        this.emailService = emailService;
        this.templateEngine = templateEngine;
        this.customerUserRepository = customerUserRepository;
        this.request=request;
        this.kafkaTemplate=kafkaTemplate;
        this.sagaTracker=sagaTracker;
    }


    /**
     * Retrieves a paginated list of customers based on the provided filter criteria and sorting options.
     * This method applies the filters specified in the {@link FilterCustomerRequestDto} to query customers
     * from the database, sorts them according to the selected field and direction, and returns the results
     * in a paginated response format.
     *
     * @param filterCustomerRequestDto the filtering, sorting, and pagination parameters to apply when retrieving customers
     * @return a {@link PaginatedCustomerResponseDto} containing the filtered and paginated list of customers
     */
    @Override
    public PaginatedCustomerResponseDto viewAllCustomers(FilterCustomerRequestDto filterCustomerRequestDto) {
        UtilityMethods.filterParameterCheckerForViewAllCustomers(filterCustomerRequestDto);
        String sortField = filterCustomerRequestDto.getSortField().getSortField();
        Sort sort = filterCustomerRequestDto.getSortDirection() == SortDirection.DESCENDING ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(filterCustomerRequestDto.getPage(), filterCustomerRequestDto.getSize(), sort);
        Page<Customer> customers = customerRepository.filterCustomers(
                filterCustomerRequestDto.getFirstName(),
                filterCustomerRequestDto.getLastName(),
                filterCustomerRequestDto.getEmail(),
                filterCustomerRequestDto.getPhoneNumber(),
                filterCustomerRequestDto.getAddress(),
                filterCustomerRequestDto.getCity(),
                filterCustomerRequestDto.getState(),
                filterCustomerRequestDto.getPostalCode(),
                filterCustomerRequestDto.getCountry(),
                filterCustomerRequestDto.getStatus(),
                pageable
        );
        return customerMapper.customerToPaginatedCustomerResponseDto(customers);
    }

    /**
     * Retrieves an active customer by the specified customer ID.
     *
     * This method fetches the customer record from the database where the customer ID matches
     * the provided value and the customer status is active.
     *
     * @param id the UUID of the customer to retrieve
     * @return the Customer entity corresponding to the given ID if found and active
     */
    @Override
    public Customer getCustomerById(UUID id) {
        return customerRepository.getCustomerById(id, Status.ACTIVE);
    }

    /**
     * Retrieves a customer based on the associated user ID.
     *
     * This method fetches the customer record linked to the specified user ID from the database.
     *
     * @param id the UUID of the user whose customer record needs to be retrieved
     * @return the Customer entity associated with the given user ID
     */
    @Override
    public Customer getCustomerByUserId(UUID id) {
        return customerRepository.findByUserId(id);
    }


    /**
     * Deletes a customer based on the associated user ID after performing necessary validations.
     *
     * This method retrieves the customer linked to the specified user ID and verifies that:
     * - The customer is active.
     * - A valid JWT token is present in the request header.
     * - The customer's account is closed and has a zero balance before deletion.
     *
     * If any of these conditions are not met, an appropriate error message is returned.
     * Otherwise, the customer record is deleted, and a success response containing the
     * customer details is returned.
     *
     * @param id the UUID of the user whose customer record needs to be deleted
     * @return an ApiResponse containing the deleted customer's details or an error message if the deletion fails
     */
    @Override
    public ApiResponse<CustomerReturnDto> deleteCustomerByUserId(UUID id) {
        ApiResponse<CustomerReturnDto> apiResponse = new ApiResponse<>();
        Customer customer = customerRepository.findByUserId(id);
        if (customer == null) {
            throw new CustomerNotFoundException(CUSTOMER_NOT_FOUND);
        }
        if(customer.getStatus() == Status.ACTIVE) {
            String authHeader = request.getHeader(AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith(BEARER)) {
                apiResponse.setStatus(STATUS_ERROR);
                apiResponse.setMessage("No JWT token received from gateway");
                return apiResponse;
            }
            String token = authHeader.substring(7);
            ApiResponse<AccountsReturnDto> customerExists = this.accountsWebclientService.getAccountData(token, customer.getCustomerId()).block();
            if (customerExists == null) {
                apiResponse.setStatus(STATUS_ERROR);
                apiResponse.setMessage("Failed to fetch account details.");
                return apiResponse;
            }
            if (STATUS_ERROR.equalsIgnoreCase(customerExists.getStatus())) {
                apiResponse.setStatus(STATUS_ERROR);
                apiResponse.setMessage(customerExists.getMessage());
                return apiResponse;
            }
            if (customerExists.getData() != null && !customerExists.getData().getStatus().equalsIgnoreCase("closed")) {
                apiResponse.setStatus(STATUS_ERROR);
                apiResponse.setMessage("Account for the customer is still active.");
                return apiResponse;
            }
            if (customerExists.getData() != null
                    && customerExists.getData().getBalance().compareTo(BigDecimal.ZERO) != 0) {
                apiResponse.setStatus(STATUS_ERROR);
                apiResponse.setMessage("Customer Account balance not 0.");
                return apiResponse;
            }
        }
            this.deleteCustomerById(customer.getCustomerId());
            CustomerReturnDto customerReturnDto = new CustomerReturnDto();
            customerReturnDto.setCustomerId(customer.getCustomerId());
            customerReturnDto.setCustomerNo(customer.getCustomerNo());
            customerReturnDto.setFirstName(customer.getFirstName());
            customerReturnDto.setLastName(customer.getLastName());
            customerReturnDto.setEmail(customer.getEmail());
            customerReturnDto.setPhoneNumber(customer.getPhoneNumber());
            customerReturnDto.setDateOfBirth(customer.getDateOfBirth());
            customerReturnDto.setAddress(customer.getAddress());
            customerReturnDto.setCity(customer.getCity());
            customerReturnDto.setState(customer.getState());
            customerReturnDto.setPostalCode(customer.getPostalCode());
            customerReturnDto.setCountry(customer.getCountry());
            customerReturnDto.setStatus(customer.getStatus().toString());
            customerReturnDto.setCreatedAt(customer.getCreatedAt());
            customerReturnDto.setUpdatedAt(customer.getUpdatedAt());
            customerReturnDto.setUserId(customer.getUserId());
            apiResponse.setStatus(STATUS_SUCCESS);
            apiResponse.setMessage(CUSTOMER_RETRIEVED);
            apiResponse.setData(customerReturnDto);
            return apiResponse;
    }

    /**
     * Deletes a customer by the specified customer ID after performing validation checks.
     *
     * This method performs the following actions:
     * - Retrieves the active customer record based on the provided ID.
     * - Validates the presence and format of the JWT token in the request header.
     * - Ensures that the customer's account is closed and has a zero balance before deletion.
     * - Publishes a customer deletion event and deletes the customer record locally.
     *
     * If any validation fails or an error occurs during deletion, appropriate exceptions are thrown.
     *
     * @param id the UUID of the customer to be deleted
     * @return an ApiResponse indicating the success of the deletion operation
     * @throws FailedToDeleteCustomerException if the customer cannot be deleted or does not exist
     * @throws SomethingWentWrongException if the JWT token is missing, invalid, or account conditions are not met
     */
    @Override
    @Transactional
    public ApiResponse<JsonObject> deleteCustomerById(UUID id) {
        Customer customer = customerRepository.getCustomerById(id, Status.ACTIVE);
        if (customer == null) {
            throw new FailedToDeleteCustomerException(FAILED_TO_DELETE_CUSTOMER);
        }

            String authHeader = request.getHeader(AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith(BEARER)) {
                throw new SomethingWentWrongException("No JWT token received from gateway");
            }
            String token = authHeader.substring(7);
            ApiResponse<AccountsReturnDto> customerExists = this.accountsWebclientService.getAccountData(token, customer.getCustomerId()).block();
            if (customerExists!=null && customerExists.getStatus().equalsIgnoreCase("error")) {
                throw new SomethingWentWrongException(customerExists.getMessage());
            }
            if(customerExists != null
                    && customerExists.getData() != null && !customerExists.getData().getStatus().equalsIgnoreCase("closed")) {
                throw new SomethingWentWrongException("Account for the customer is still active.");
            }
            if (customerExists != null
                    && customerExists.getData() != null
                    && customerExists.getData().getBalance().compareTo(BigDecimal.ZERO) != 0) {
                throw new SomethingWentWrongException("Customer Account balance not 0.");
            }
            try {
                publishCustomerDeletedEvent(customer);

                // 2. Then delete the customer locally
                customerRepository.delete(customer);

                // 3. Return success response
                ApiResponse<JsonObject> apiResponse = new ApiResponse<>();
                apiResponse.setStatus(STATUS_SUCCESS);
                apiResponse.setMessage("Customer Deleted Successfully.");

                return apiResponse;
            } catch (RuntimeException ex) {
                throw new FailedToDeleteCustomerException(FAILED_TO_DELETE_CUSTOMER + ": " + ex.getMessage());
            }

    }

    /**
     * Publishes a customer deletion event to the Kafka topic.
     *
     * This method constructs a CustomerDeletedEvent object containing relevant customer details
     * such as customer ID, user ID, and customer number. It then sends the event asynchronously
     * to the "customer.deleted" Kafka topic.
     *
     * On successful publication, an informational log is recorded with the event ID and offset.
     * If the publication fails, an error log with the exception message is recorded.
     *
     * @param customer the Customer entity whose deletion event is to be published
     */
    private void publishCustomerDeletedEvent(Customer customer) {
        CustomerDeletedEvent event = new CustomerDeletedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventName("customer.deleted");
        event.setCustomerId(customer.getCustomerId());
        event.setUserId(customer.getUserId());
        event.setCustomerNo(customer.getCustomerNo());
        event.setReason("Customer deletion requested");
        event.setTimeStamp(Instant.now().toString());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("customer.deleted", event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Customer deleted event published: {} with offset: {}",
                        event.getCustomerId(), result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to publish customer deleted event for customer {}: {}",
                        event.getCustomerId(), ex.getMessage());
            }
        });
    }

    /**
     * Creates a new customer and coordinates the customer creation process across multiple services
     * using the Saga pattern with circuit breaker and retry mechanisms.
     *
     * This method performs the following steps:
     * 1. Validates the JWT token received in the request header.
     * 2. Sends the customer data to the User Service for validation.
     * 3. Creates and saves a new customer record in the local database.
     * 4. Publishes a customer creation event with a correlation ID to coordinate downstream services.
     * 5. Waits for the saga to complete successfully within a 30-second timeout period.
     * 6. Rolls back the operation if the saga fails, times out, or an exception occurs during the process.
     *
     * If the saga completes successfully, the saved customer details are returned as a response DTO.
     * If any error occurs, a rollback is triggered and an exception is thrown.
     *
     * @param customerDTO the data transfer object containing the customer's information
     * @return a CustomerResponseDTO containing the created customer's details
     * @throws SomethingWentWrongException if the JWT token is missing or user service validation fails
     * @throws RuntimeException if the saga fails, times out, or an exception occurs during the creation process
     */
    @Override
    @Transactional
    @CircuitBreaker(name = "customerService", fallbackMethod = "createCustomerFallback")
    @Retry(name = "customerService")
    public CustomerResponseDTO createCustomer(CustomerDto customerDTO) {
        String authHeader = request.getHeader(AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            throw new SomethingWentWrongException("No JWT token received from gateway");
        }
        String token = authHeader.substring(7);
        Boolean userOk = userWebClientService.sendCustomerDataToUserService(customerDTO, token)
                .doOnSuccess(result -> logger.debug("User service validation successful"))
                .doOnError(throwable -> logger.warn("User service call failed - will trigger retry: {}", throwable.getMessage()))
                .onErrorResume(Mono::error)
                .block();

        if (Boolean.FALSE.equals(userOk)) {
            throw new SomethingWentWrongException("Email or phone number already exists in user-service");
        }

        // 2. Create customer entity
        Customer customerEntity = createCustomerEntity(customerDTO);
        Customer savedCustomer = customerRepository.save(customerEntity);
        customerRepository.flush();
        String correlationId = sagaTracker.registerSaga(savedCustomer.getCustomerNo());

        try {
            // 3. Publish event with correlation ID
            publishCustomerCreatedEventWithCorrelation(savedCustomer, correlationId);

            // 4. WAIT for saga completion (30 seconds timeout)
            boolean sagaSuccess = sagaTracker.waitForSagaCompletion(correlationId, 30);

            if (!sagaSuccess) {
                // Rollback everything if saga fails or times out
                String rollbackReason = "Downstream services failed to complete";
                rollbackCompleteCustomerCreation(savedCustomer, rollbackReason);

                // THROW EXCEPTION to prevent success response
                throw new FailedToCreateCustomerException("Customer creation failed: " + rollbackReason);
            }

            logger.info("Saga completed successfully for customer: {}", savedCustomer.getCustomerNo());
            return convertToResponseDTO(savedCustomer);

        } catch (RuntimeException e) {
            // If any exception occurs during saga execution
            // Make sure to rollback if not already done
            try {
                rollbackCompleteCustomerCreation(savedCustomer, "Exception during creation: " + e.getMessage());
            } catch (RuntimeException rollbackEx) {
                logger.error("Rollback also failed: {}", rollbackEx.getMessage());
            }

            throw new FailedToCreateCustomerException("Customer creation failed"+ e);
        }
    }

    /**
     * Updates the user ID for a customer after successful user creation and sends a confirmation email.
     *
     * This asynchronous method performs the following steps:
     * 1. Retrieves the CustomerUserEntity linked to the given customer ID.
     * 2. Updates the corresponding Customer record with the user ID from the CustomerUserEntity.
     * 3. Fetches account details for the customer using the Accounts Service.
     * 4. Constructs a CustomerEmailDto containing customer and account information.
     * 5. Sends a confirmation email to the customer with the relevant details.
     *
     * If the account does not exist or any step fails, a RuntimeException is thrown.
     *
     * @param token the JWT token used for authentication when calling the Accounts Service
     * @param customerId the UUID of the customer whose user ID needs to be updated
     * @throws RuntimeException if the account does not exist or email sending fails
     */
    @Override
    @Async
    public void updateUserId(String token, UUID customerId){
        CustomerUserEntity customerUserEntity = this.customerUserRepository.findByCustomerId(customerId);
        Customer customer = this.customerRepository.findByCustomerIdAndStatus(customerId, Status.ACTIVE);
        customer.setUserId(customerUserEntity.getUserId());
        this.customerRepository.save(customer);
        CustomerEmailDto customerEmailDto = new CustomerEmailDto();
        customerEmailDto.setEmail(customer.getEmail());
        customerEmailDto.setPhoneNumber(customer.getPhoneNumber());
        customerEmailDto.setUserName(customerUserEntity.getUserName());
        customerEmailDto.setFirstName(customer.getFirstName());
        customerEmailDto.setLastName(customer.getLastName());
        ApiResponse<AccountsReturnDto> customerExists = this.accountsWebclientService.getAccountData(token, customer.getCustomerId()).block();
        if(customerExists !=null){
            customerEmailDto.setAccountType(customerExists.getData().getAccountType());
            customerEmailDto.setAccountNumber(customerExists.getData().getAccountNumber());
        }
        else{
            throw new FailedToUpdateCustomerException("Account doesnot exist.");
        }
        try {
            this.emailCustomerCreate(customerEmailDto);
        } catch (RuntimeException e) {
            throw new FailedToUpdateCustomerException(e.getMessage());
        }
    }

    /**
     * Creates a new Customer entity from the provided CustomerDto.
     * This method maps the customer details from the CustomerDto to a new Customer entity,
     * generates a unique customer number, and sets the customer's initial status to ACTIVE.
     *
     * @param customerDTO the data transfer object containing customer details
     * @return a newly created Customer entity populated with the provided information
     */
    private Customer createCustomerEntity(CustomerDto customerDTO) {
        Customer customerEntity = new Customer();
        customerEntity.setFirstName(customerDTO.getFirstName());
        customerEntity.setLastName(customerDTO.getLastName());
        customerEntity.setEmail(customerDTO.getEmail());
        customerEntity.setPhoneNumber(customerDTO.getPhoneNumber());
        customerEntity.setAddress(customerDTO.getAddress());
        customerEntity.setCity(customerDTO.getCity());
        customerEntity.setState(customerDTO.getState());
        customerEntity.setCountry(customerDTO.getCountry());
        customerEntity.setPostalCode(customerDTO.getPostalCode());
        customerEntity.setDateOfBirth(customerDTO.getDateOfBirth());

        Long customerNo = generateNextCustomerNumber();
        customerEntity.setCustomerNo(customerNo);
        customerEntity.setStatus(Status.ACTIVE);

        return customerEntity;
    }

    /**
     * Publishes a customer created event to the Kafka topic with a correlation ID for saga tracking.
     * This method constructs a CustomerCreatedEvent containing all relevant customer details
     * and the provided correlation ID, then sends it synchronously to the configured Kafka topic.
     *
     * @param customer the Customer entity whose creation event is to be published
     * @param correlationId the unique correlation ID used to track the saga across services
     * @throws RuntimeException if the event fails to be published to Kafka
     */
    private void publishCustomerCreatedEventWithCorrelation(Customer customer, String correlationId) {
        try {
            CustomerCreatedEvent event = new CustomerCreatedEvent();
            event.setEventId(UUID.randomUUID().toString());
            event.setEventName(CUSTOMER_CREATED_TOPIC);
            event.setTimeStamp(Instant.now().toString());
            event.setCustomerId(customer.getCustomerId());
            event.setCustomerNo(customer.getCustomerNo());
            event.setFirstName(customer.getFirstName());
            event.setLastName(customer.getLastName());
            event.setEmail(customer.getEmail());
            event.setPhoneNumber(customer.getPhoneNumber());
            event.setStatus(customer.getStatus().name());
            event.setCreatedAt(customer.getCreatedAt().toString());
            event.setCity(customer.getCity());
            event.setAddress(customer.getAddress());
            event.setCountry(customer.getCountry());
            event.setState(customer.getState());
            event.setPostalCode(customer.getPostalCode());
            event.setDateOfBirth(customer.getDateOfBirth());

            // Add correlation ID for tracking
            event.setCorrelationId(correlationId);

            kafkaTemplate.send(CUSTOMER_CREATED_TOPIC, event).get();
            logger.info("Customer created event published with correlation: {}", correlationId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FailedToPublishEventException("Interrupted while publishing customer created event");
        } catch (ExecutionException | RuntimeException e) {
            throw new FailedToPublishEventException("Failed to publish customer created event"+e);
        }
    }

    /**
     * Sends a customer onboarding email using the provided customer details.
     *
     * This method prepares the email content by populating a template with placeholders
     * such as the customer's name, username, email, phone number, account number, and account type.
     * It then processes the HTML template and sends the email using the configured email service.
     *
     * @param customerEmailDto the data transfer object containing customer details for the email
     * @throws Exception if there is an error while processing the template or sending the email
     */
    private void emailCustomerCreate(CustomerEmailDto customerEmailDto) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("name", customerEmailDto.getFirstName() + " " + customerEmailDto.getLastName());
        placeholders.put("userName", customerEmailDto.getUserName());
        placeholders.put("email", customerEmailDto.getEmail());
        placeholders.put("phoneNumber", customerEmailDto.getPhoneNumber());
        placeholders.put("accountNumber", customerEmailDto.getAccountNumber());
        placeholders.put("accountType", customerEmailDto.getAccountType());
        placeholders.put("link", registerFrontendUrl);
        try {
            Context context = new Context();
            context.setVariables(placeholders);

            // This processes the template and replaces th:text and th:href variables
            String htmlContent = templateEngine.process("customerOnboard.html", context);
            emailService.sendHtml(customerEmailDto.getEmail(), "Account Has Been Created", htmlContent);
        } catch (RuntimeException e) {
            throw new FailedToSendEmailException(e.getMessage());
        }
    }

    /**
     * Converts a Customer entity to a CustomerResponseDTO.
     * This method maps all relevant fields from the Customer entity to a
     * CustomerResponseDTO, including mandatory and optional fields.
     *
     * @param customer the Customer entity to be converted
     * @return a CustomerResponseDTO containing the mapped customer information
     */
    private CustomerResponseDTO convertToResponseDTO(Customer customer) {
        CustomerResponseDTO responseDTO = new CustomerResponseDTO();

        // Map all fields from Customer entity to CustomerResponseDTO
        responseDTO.setCustomerId(customer.getCustomerId());
        responseDTO.setCustomerNo(customer.getCustomerNo());
        responseDTO.setFirstName(customer.getFirstName());
        responseDTO.setLastName(customer.getLastName());
        responseDTO.setEmail(customer.getEmail());
        responseDTO.setPhoneNumber(customer.getPhoneNumber());
        responseDTO.setStatus(Status.ACTIVE); // Convert enum to string
        responseDTO.setCreatedAt(customer.getCreatedAt());
        responseDTO.setUpdatedAt(customer.getUpdatedAt());
        responseDTO.setUserId(customer.getUserId());

        // Optional fields
        responseDTO.setAddress(customer.getAddress());
        responseDTO.setCity(customer.getCity());
        responseDTO.setState(customer.getState());
        responseDTO.setCountry(customer.getCountry());
        responseDTO.setPostalCode(customer.getPostalCode());
        return responseDTO;
    }

    /**
     * Rolls back the customer creation process in a new transaction.
     * This method ensures that the rollback of a customer identified by the given customer number
     * is executed in a separate transaction, independent of the current transaction context.
     *
     * @param customerNo the customer number of the customer to rollback
     * @param reason the reason for rolling back the customer creation
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rollbackCustomerInNewTransaction(Long customerNo, String reason) {
        rollbackCustomer(customerNo, reason);
    }

    /**
     * Updates an existing customer's details based on the provided CustomerDto.
     * This method performs the following steps:
     * 1. Retrieves the customer by the provided customer number.
     * 2. Updates the customer's fields with the values from the CustomerDto.
     * 3. Sets the status based on the DTO value ("ACTIVE" or "INACTIVE").
     * 4. Updates the timestamp for the last modification.
     * 5. Saves the updated customer entity to the database.
     * 6. Publishes a customer updated event to notify downstream services.
     *
     * If the customer is not found, a RuntimeException is thrown.
     * If the update succeeds but event publishing fails, a RuntimeException is also thrown.
     *
     * @param customerNo the customer number of the customer to update
     * @param customerDTO the data transfer object containing updated customer details
     * @return a CustomerResponseDTO containing the updated customer information
     * @throws RuntimeException if the customer is not found or event publishing fails
     */
    @Override
    @CircuitBreaker(name = "customerService", fallbackMethod = "updateCustomerFallback")
    @Retry(name = "customerService")
    public CustomerResponseDTO updateCustomer(Long customerNo, CustomerDto customerDTO) {
        Optional<Customer> customerOptional = customerRepository.findByCustomerNo(customerNo);
        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();

            // Map values from DTO to entity
            customer.setFirstName(customerDTO.getFirstName());
            customer.setLastName(customerDTO.getLastName());
            customer.setEmail(customerDTO.getEmail());
            customer.setPhoneNumber(customerDTO.getPhoneNumber());
            if(customerDTO.getStatus().equals("ACTIVE")){
                customer.setStatus(Status.ACTIVE);
            }
            else{
                customer.setStatus(Status.INACTIVE);
            }
            customer.setUpdatedAt(LocalDateTime.now());
            customer.setDateOfBirth(customerDTO.getDateOfBirth());
            customer.setCity(customerDTO.getCity());
            customer.setState(customerDTO.getState());
            customer.setCountry(customerDTO.getCountry());
            customer.setPostalCode(customerDTO.getPostalCode());
            customer.setAddress(customerDTO.getAddress());

            Customer updatedCustomer = customerRepository.save(customer);

            try {
                publishCustomerUpdatedEvent(updatedCustomer);

                logger.info("Customer updated successfully: {}", customerNo);
                return customerMapper.toResponseDTO(updatedCustomer);

            } catch (RuntimeException e) {
                throw new FailedToPublishEventException("Customer updated but event publishing failed"+e);
            }

        } else {
            throw new CustomerNotFoundException("Customer not found with number: " + customerNo);
        }
    }

    /**
     * Circuit-breaker fallback for {@link #createCustomer(CustomerDto)}.
     * Resilience4j calls it for every failure, so business errors are rethrown unchanged;
     * only an open circuit is reported as "service unavailable".
     *
     * @param customerDTO the original request
     * @param t the failure that triggered the fallback
     * @return never returns normally
     */
    public CustomerResponseDTO createCustomerFallback(CustomerDto customerDTO, Throwable t) {
        throw fallbackException("Customer creation", t, FailedToCreateCustomerException::new);
    }

    /**
     * Circuit-breaker fallback for {@link #updateCustomer(Long, CustomerDto)}.
     *
     * @param customerNo the customer number being updated
     * @param customerDTO the original request
     * @param t the failure that triggered the fallback
     * @return never returns normally
     */
    public CustomerResponseDTO updateCustomerFallback(Long customerNo, CustomerDto customerDTO, Throwable t) {
        throw fallbackException("Customer update", t, FailedToUpdateCustomerException::new);
    }

    private RuntimeException fallbackException(String operation, Throwable t,
                                               Function<String, RuntimeException> unavailable) {
        if (t instanceof CallNotPermittedException) {
            logger.error("{} circuit open: {}", operation, t.getMessage());
            return unavailable.apply(operation + " service unavailable: " + t.getMessage());
        }
        if (t instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        return unavailable.apply(operation + " failed: " + t.getMessage());
    }

    /**
     * Publishes a customer updated event to the Kafka topic.
     *
     * This method constructs a CustomerUpdatedEvent containing updated customer details
     * and sends it asynchronously to the configured Kafka topic. Upon completion, it logs
     * either the success with the Kafka offset or an error if publishing fails.
     *
     * @param customer the Customer entity whose updated details are to be published
     */
    private void publishCustomerUpdatedEvent(Customer customer) {
        CustomerUpdatedEvent event = new CustomerUpdatedEvent();
        // Set only the fields that exist in CustomerUpdatedEvent
        event.setCustomerNo(customer.getCustomerNo());
        event.setFirstName(customer.getFirstName());
        event.setLastName(customer.getLastName());
        event.setEmail(customer.getEmail());
        event.setPhoneNumber(customer.getPhoneNumber());
        event.setStatus(customer.getStatus().name());
        event.setUpdatedAt(customer.getUpdatedAt().toString());
        event.setAddress(customer.getAddress());
        event.setCity(customer.getCity());
        event.setCountry(customer.getCountry());
        event.setDateOfBirth(customer.getDateOfBirth());
        event.setPostalCode(customer.getPostalCode());
        event.setState(customer.getState());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(CUSTOMER_UPDATED_TOPIC, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Customer updated event published: {} with offset: {}",
                        event.getCustomerNo(), result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to publish customer updated event for customer {}: {}",
                        event.getCustomerNo(), ex.getMessage());
            }
        });
    }

    /**
     * Publishes a customer rollback event to the Kafka topic.
     *
     * This method constructs a CustomerRollbackEvent containing the user ID and the
     * reason for rollback, then sends it asynchronously to the configured Kafka topic.
     * Upon completion, it logs either the success of the event publishing or an error
     * if publishing fails.
     *
     * @param userId the UUID of the user whose customer creation is being rolled back
     * @param reason the reason for rolling back the customer creation
     */
    private void publishCustomerRollbackEvent(UUID userId, String reason) {
        CustomerRollbackEvent event = new CustomerRollbackEvent();
        // Set only the fields that exist in CustomerRollbackEvent
        event.setUserId(userId);
        event.setReason(reason);
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(CUSTOMER_ROLLBACK_TOPIC, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Customer rollback event published: {}", userId);
            } else {
                logger.error("Failed to publish customer rollback event for customer {}: {}",
                        userId, ex.getMessage());
            }
        });
    }

    /**
     * Generates the next unique customer number using a database sequence.
     *
     * This method retrieves the next value from the 'customer_no_seq' sequence
     * in the 'customer_schema' schema and returns it as a Long.
     *
     * @return the next unique customer number
     */
    private Long generateNextCustomerNumber() {
        Query query = entityManager.createNativeQuery("SELECT nextval('customer_schema.customer_no_seq')");
        return ((Number) query.getSingleResult()).longValue();
    }

    /**
     * Retrieves a list of all active customers.
     *
     * This method queries the customer repository to return all customers
     * whose status is set to ACTIVE.
     *
     * @return a list of active Customer entities
     */
    @Override
    public List<Customer> getCustomerCount(){
        return this.customerRepository.findAllByStatus(Status.ACTIVE);
    }

    /**
     * Deletes a customer and their associated account based on the provided email.
     *
     * This method performs the following steps:
     * 1. Retrieves the customer entity using the given email.
     * 2. Deletes the associated account via the Accounts Service if the customer exists.
     * 3. Deletes the customer record from the local repository.
     *
     * @param email the email of the customer to be deleted
     */
    @Override
    public void deleteByEmail(String email){
        Customer customer = this.customerRepository.findByEmail(email);
        if(customer.getCustomerId() != null) {
            this.accountsServiceClient.deleteAccount(customer.getCustomerId());
            customerRepository.delete(customer);
        }
    }

    /**
     * Performs a complete rollback of a customer creation process across all services.
     *
     * This method executes the rollback in the following order:
     * 1. Rolls back the local customer record.
     * 2. Triggers a rollback in the User Service via Kafka.
     * 3. Triggers a rollback in the Account Service.
     *
     * Any exceptions during the rollback process are caught and logged.
     *
     * @param customer the Customer entity for which the rollback is being performed
     * @param reason the reason for rolling back the customer creation
     */
    private void rollbackCompleteCustomerCreation(Customer customer, String reason) {
        try {
            // 1. First rollback local customer
            rollbackCustomer(customer.getCustomerNo(), reason);

            // 2. Then trigger user service rollback via Kafka
            triggerUserRollback(customer.getEmail(), customer.getPhoneNumber(), reason);

            // 3. Also trigger account service rollback
            triggerAccountRollback(customer.getCustomerNo(), reason);

            logger.warn("Complete rollback executed for customer: {} - Reason: {}",
                    customer.getCustomerNo(), reason);

        } catch (RuntimeException rollbackEx) {
            logger.error("Complete rollback failed for customer {}: {}",
                    customer.getCustomerNo(), rollbackEx.getMessage());
        }
    }

    /**
     * Rolls back a customer by deleting the customer record and publishing a rollback event.
     *
     * This method performs the following steps:
     * 1. Retrieves the customer by the provided customer number.
     * 2. Deletes the customer record from the repository and flushes to ensure immediate removal.
     * 3. Logs a warning with the rollback reason.
     * 4. Publishes a customer rollback event for auditing purposes.
     *
     * If the customer is not found, a warning is logged. Any exceptions during the rollback
     * process are logged and rethrown as a RuntimeException.
     *
     * @param customerNo the customer number of the customer to rollback
     * @param reason the reason for rolling back the customer
     * @throws RuntimeException if the rollback fails due to an unexpected error
     */
    public void rollbackCustomer(Long customerNo, String reason) {
        try {
            Optional<Customer> customerOptional = customerRepository.findByCustomerNo(customerNo);

            if (customerOptional.isPresent()) {
                Customer customer = customerOptional.get();
                UUID userId = customer.getUserId();
                customerRepository.delete(customer);
                customerRepository.flush(); // Ensure immediate deletion

                logger.warn("Customer rolled back: {} - Reason: {}", customerNo, reason);

                // Also publish customer rollback event for auditing
                publishCustomerRollbackEvent(userId, reason);
            } else {
                logger.warn("Customer not found for rollback: {}", customerNo);
            }
        } catch (RuntimeException e) {
            throw new CustomerRollbackFailedException("Customer rollback failed"+e);
        }
    }

    /**
     * Triggers a rollback event for the User Service via Kafka.
     *
     * This method constructs a UserRollbackEvent containing the user's email, phone number,
     * and the reason for rollback, then sends it synchronously to the configured Kafka topic.
     * Logs are generated to indicate the sending status and any errors encountered.
     *
     * @param email the email of the user associated with the rollback
     * @param phoneNumber the phone number of the user associated with the rollback
     * @param reason the reason for triggering the user rollback
     */
    private void triggerUserRollback(String email, String phoneNumber, String reason) {
        try {
            UserRollbackEvent event = new UserRollbackEvent();
            event.setEventName(CUSTOMER_ROLLBACK_TOPIC);
            event.setEmail(email);
            event.setPhoneNumber(phoneNumber);
            event.setReason(reason);

            logger.info("🚀 SENDING USER ROLLBACK EVENT - Email: {}, Phone: {}", email, phoneNumber);

            // Send synchronously to ensure delivery
            SendResult<String, Object> result = kafkaTemplate.send(CUSTOMER_ROLLBACK_TOPIC, event).get(10, TimeUnit.SECONDS);

            logger.info("✅ USER ROLLBACK EVENT SENT SUCCESSFULLY - Email: {}, Topic: {}, Partition: {}",
                    email, result.getRecordMetadata().topic(), result.getRecordMetadata().partition());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while sending user rollback event - Email: {}", email, e);
        } catch (ExecutionException | TimeoutException | RuntimeException e) {
            logger.error("❌ FAILED TO SEND USER ROLLBACK EVENT - Email: {} - Error: {}", email, e.getMessage(), e);
        }
    }

    /**
     * Triggers a rollback event for the Account Service via Kafka.
     *
     * This method constructs an AccountRollbackEvent containing the customer number
     * and the reason for rollback, then sends it asynchronously to the configured Kafka topic.
     * Logs are generated to indicate success or failure of the event publishing.
     *
     * @param customerNo the customer number associated with the account rollback
     * @param reason the reason for triggering the account rollback
     */
    private void triggerAccountRollback(Long customerNo, String reason) {
        try {
            AccountRollbackEvent event = new AccountRollbackEvent();
            event.setCustomerNo(customerNo);
            event.setReason(reason);

            kafkaTemplate.send("account-rollback", event);
            logger.info("Account rollback event published for customer: {}", customerNo);

        } catch (RuntimeException e) {
            logger.error("Failed to publish account rollback event for customer: {} - {}", customerNo, e.getMessage());
        }
    }
}
