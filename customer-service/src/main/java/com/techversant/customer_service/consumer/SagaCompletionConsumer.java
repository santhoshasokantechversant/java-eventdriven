/**
 * @file SagaCompletionConsumer.java
 * @company Techversant Infotech
 * @author Siya Elsa Sabu
 * @version 1.0
 * @description This component listens to Saga-related Kafka events and updates the saga tracker for user and account service completion or failure.
 */

package com.techversant.customer_service.consumer;

import com.techversant.common_lib.events.AccountCreatedEvent;
import com.techversant.common_lib.events.AccountCreationFailedEvent;
import com.techversant.common_lib.events.UserCreatedEvent;
import com.techversant.common_lib.events.UserCreationFailedEvent;
import com.techversant.customer_service.service.CustomerCreationSagaTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SagaCompletionConsumer {

    private final CustomerCreationSagaTracker sagaTracker;

    public SagaCompletionConsumer(CustomerCreationSagaTracker sagaTracker) {
        this.sagaTracker = sagaTracker;
    }

    private static final Logger logger = LoggerFactory.getLogger(SagaCompletionConsumer.class);

    /**
     * Kafka listener for the "user.created" topic in the customer service saga.
     * This method is triggered when a user is successfully created. It updates the
     * {@link CustomerCreationSagaTracker} to mark the user service step as completed
     * for the given correlation ID.
     * Logs the completion status for auditing and debugging purposes.
     *
     * @param event the {@link UserCreatedEvent} containing the correlation ID and user details
     */
    @KafkaListener(topics = "user.created", groupId = "customer-service-saga-group")
    public void consumeUserCreated(UserCreatedEvent event) {
        if (event.getCorrelationId() != null) {
            sagaTracker.markUserServiceCompleted(event.getCorrelationId(), true);
            logger.info("User service completed for correlation: {}", event.getCorrelationId());
        }
    }

    /**
     * Kafka listener for the "user.creation.failed" topic in the customer service saga.
     * This method is triggered when a user creation operation fails. It updates the
     * {@link CustomerCreationSagaTracker} to mark the user service step as failed
     * for the given correlation ID.
     * Logs the failure with the correlation ID for monitoring and debugging purposes.
     *
     * @param event the {@link UserCreationFailedEvent} containing the correlation ID and failure details
     */
    @KafkaListener(topics = "user.creation.failed", groupId = "customer-service-saga-group")
    public void consumeUserCreationFailed(UserCreationFailedEvent event) {
        if (event.getCorrelationId() != null) {
            sagaTracker.markUserServiceCompleted(event.getCorrelationId(), false);
            logger.error("User service failed for correlation: {}", event.getCorrelationId());
        }
    }

    /**
     * Kafka listener for the "account.created" topic in the customer service saga.
     * This method is triggered when an account is successfully created. It updates the
     * {@link CustomerCreationSagaTracker} to mark the account service step as completed
     * for the given correlation ID.
     * Logs the completion status for auditing and debugging purposes.
     *
     * @param event the {@link AccountCreatedEvent} containing the correlation ID and account details
     */
    @KafkaListener(topics = "account.created", groupId = "customer-service-saga-group")
    public void consumeAccountCreated(AccountCreatedEvent event) {
        if (event.getCorrelationId() != null) {
            sagaTracker.markAccountServiceCompleted(event.getCorrelationId(), true);
            logger.info("Account service completed for correlation: {}", event.getCorrelationId());
        }
    }

    /**
     * Kafka listener for the "account.creation.failed" topic in the customer service saga.
     * This method is triggered when an account creation operation fails. It updates the
     * {@link CustomerCreationSagaTracker} to mark the account service step as failed
     * for the given correlation ID.
     * Logs both an informative message and an error for monitoring, debugging, and auditing purposes.
     *
     * @param event the {@link AccountCreationFailedEvent} containing the customer number and correlation ID
     */
    @KafkaListener(topics = "account.creation.failed", groupId = "customer-service-saga-group")
    public void consumeAccountCreationFailed(AccountCreationFailedEvent event) {
        logger.info("🎯 CUSTOMER SERVICE RECEIVED ACCOUNT FAILURE - Customer: {}, Correlation: {}",
                event.getCustomerNo(), event.getCorrelationId());

        if (event.getCorrelationId() != null) {
            sagaTracker.markAccountServiceCompleted(event.getCorrelationId(), false);
            logger.error("Account service failed for correlation: {}", event.getCorrelationId());
        }
    }
}