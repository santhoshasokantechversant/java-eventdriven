/**
 * @file AccountEventConsumer.java
 * @company Techversant Infotech
 * @author Siya Elsa Sabu
 * @version 1.0
 * @description Kafka event consumer class
 */

package com.techversant.accountservice.events;

import com.techversant.accountservice.dto.AccountDto;
import com.techversant.accountservice.model.ConsumedEvent;
import com.techversant.accountservice.repository.ConsumedEventRepository;
import com.techversant.accountservice.service.impl.AccountService;
import com.techversant.common_lib.events.AccountCreatedEvent;
import com.techversant.common_lib.events.AccountCreationFailedEvent;
import com.techversant.common_lib.events.CustomerCreatedEvent;
import com.techversant.common_lib.events.CustomerDeletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class AccountEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AccountEventConsumer.class);

    private final AccountService accountService;
    private final AccountEventProducer accountEventProducer;
    private final ConsumedEventRepository consumedEventRepository;

    public AccountEventConsumer(AccountService accountService, AccountEventProducer accountEventProducer, ConsumedEventRepository consumedEventRepository) {
        this.accountService = accountService;
        this.accountEventProducer = accountEventProducer;
        this.consumedEventRepository = consumedEventRepository;
    }

    /**
     * Consumes "user.created" events from Kafka and creates a corresponding account.
     * This Kafka listener method:
     * - Listens to the "user.created" topic using the "customerCreatedListenerFactory".
     * - Checks if the event has already been processed using consumedEventRepository to avoid duplicates.
     * - Creates a new account with default values (SAVINGS, INR, balance 1000.00) for the customer.
     * - Persists the consumed event to track processed events.
     * - Publishes an "account.created" event upon successful account creation.
     * - Publishes an "account.creation.failed" event if account creation fails, with failure reason.
     * - Logs all key steps and errors for monitoring and debugging.
     *
     * @param event the CustomerCreatedEvent payload received from Kafka, containing customer details and correlation ID
     */
    @KafkaListener(topics = "user.created", groupId = "account-service-group", containerFactory = "customerCreatedListenerFactory")
    public void consumeUserCreated(@Payload CustomerCreatedEvent event) {
        if (consumedEventRepository.existsByEventId(event.getEventId())) {
            logger.info("Event already processed by account service: {}", event.getEventId());
            return;
        }

        try {
            logger.info("Received user created event: {} with correlation: {}",
                    event.getCustomerNo(), event.getCorrelationId());

            AccountDto accDto = new AccountDto();
            accDto.setCustomerNo(event.getCustomerNo());
            accDto.setAccountType("SAVINGS");
            accDto.setBalance(new BigDecimal("1000.00"));
            accDto.setCurrency("INR");
            accDto.setCustomerId(event.getCustomerId());

            accountService.createAccount(accDto);
            logger.info("AccountDto values - customerId: {}, accountNumber: {}",
                    accDto.getCustomerId(), accDto.getAccountNumber());

            ConsumedEvent consumedEvent = new ConsumedEvent(event.getEventId(), event.getEventName());
            consumedEventRepository.save(consumedEvent);

            AccountCreatedEvent successEvent = new AccountCreatedEvent();
            successEvent.setCustomerNo(event.getCustomerNo());
            successEvent.setEventId(UUID.randomUUID().toString());
            successEvent.setEventName("account.created");
            successEvent.setTimeStamp(Instant.now().toString());
            successEvent.setCorrelationId(event.getCorrelationId());
            accountEventProducer.sendAccountCreatedEvent(successEvent);

            logger.info("Account created successfully for customer: {}", event.getCustomerNo());
        } catch (RuntimeException e) {
            logger.error("Failed to create account for customer {}: {}", event.getCustomerNo(), e.getMessage());

            AccountCreationFailedEvent failedEvent = new AccountCreationFailedEvent();
            failedEvent.setCustomerNo(event.getCustomerNo());
            failedEvent.setReason("Account creation failed: " + e.getMessage());
            failedEvent.setEventId(UUID.randomUUID().toString());
            failedEvent.setTimeStamp(Instant.now().toString());
            failedEvent.setCorrelationId(event.getCorrelationId());
            accountEventProducer.sendAccountCreationFailedEvent(failedEvent);
        }
    }

    /**
     * Consumes "customer.deleted" events from Kafka and deletes associated accounts.
     * This Kafka listener method:
     * - Listens to the "customer.deleted" topic using the "customerDeletedListenerFactory".
     * - Receives a CustomerDeletedEvent containing the customer ID.
     * - Deletes all accounts associated with the given customer via accountService.
     * - Logs the successful deletion or any errors encountered during processing.
     *
     * @param event the CustomerDeletedEvent payload received from Kafka, containing the customer ID
     */
    @KafkaListener(topics = "customer.deleted", groupId = "account-service-group", containerFactory = "customerDeletedListenerFactory")
    public void consumeCustomerDeleted(CustomerDeletedEvent event) {
        try {
            log.info("Received customer deleted event for customer: {}", event.getCustomerId());

            accountService.deleteFromCustomer(event.getCustomerId());
            log.info("Successfully deleted accounts for customer: {}", event.getCustomerId());

        } catch (RuntimeException e) {
            log.error("Failed to delete accounts for customer {}: {}", event.getCustomerId(), e.getMessage());
        }
    }
}

