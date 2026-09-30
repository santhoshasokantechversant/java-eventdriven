/**
 * @file AccountEventProducer.java
 * @company Techversant Infotech
 * @author Siya Elsa Sabu
 * @version 1.0
 * @description Kafka event producer class
 */

package com.techversant.accountservice.events;

import com.techversant.accountservice.utils.exceptions.FailedToPublishEventException;
import com.techversant.common_lib.events.AccountCreatedEvent;
import com.techversant.common_lib.events.AccountCreationFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class AccountEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(AccountEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.account-created}")
    private String accountCreatedTopic;

    @Value("${app.kafka.topic.account-creation-failed}")
    private String accountCreationFailedTopic;

    public AccountEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes an AccountCreatedEvent to Kafka.
     * This method:
     * - Ensures the event has proper metadata: eventId, eventName, and timeStamp.
     * - Sends the event synchronously to the configured Kafka topic using KafkaTemplate.
     * - Logs the successful send including customer number, correlation ID, and event ID.
     * - Handles InterruptedException by logging and re-interrupting the thread.
     * - Handles other exceptions by logging and throwing a RuntimeException to indicate failure.
     *
     * @param event the AccountCreatedEvent containing account creation details to be published to Kafka
     * @throws RuntimeException if sending the event to Kafka fails
     */
    public void sendAccountCreatedEvent(AccountCreatedEvent event) {
        try {
            if (event.getEventId() == null) {
                event.setEventId(UUID.randomUUID().toString());
            }
            if (event.getEventName() == null) {
                event.setEventName("account.created");
            }
            if (event.getTimeStamp() == null) {
                event.setTimeStamp(Instant.now().toString());
            }

            kafkaTemplate.send(accountCreatedTopic, event).get();

            logger.info("Sent AccountCreatedEvent for customer: {}, correlation: {}, eventId: {}",
                    event.getCustomerNo(), event.getCorrelationId(), event.getEventId());

        } catch (InterruptedException e) {
            logger.error("Interrupted while sending AccountCreatedEvent for customer: {}",
                    event.getCustomerNo(), e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException | RuntimeException e) {
            throw new FailedToPublishEventException("Failed to publish account created event :"+e.getMessage());
        }
    }

    /**
     * Publishes an AccountCreationFailedEvent to Kafka.
     * This method:
     * - Ensures the event has proper metadata: eventId, eventName, and timeStamp.
     * - Sends the event synchronously to the configured Kafka topic using KafkaTemplate.
     * - Logs the successful send including customer number, correlation ID, and failure reason.
     * - Handles InterruptedException by logging and re-interrupting the thread.
     * - Handles other exceptions by logging the failure without rethrowing, as this is a failure notification.
     *
     * @param event the AccountCreationFailedEvent containing account creation failure details to be published to Kafka
     */
    public void sendAccountCreationFailedEvent(AccountCreationFailedEvent event) {
        try {
            // Ensure event has proper ID and metadata
            if (event.getEventId() == null) {
                event.setEventId(UUID.randomUUID().toString());
            }
            if (event.getEventName() == null) {
                event.setEventName("account.creation.failed");
            }
            if (event.getTimeStamp() == null) {
                event.setTimeStamp(Instant.now().toString());
            }

            kafkaTemplate.send(accountCreationFailedTopic, event).get();
            logger.info("Sent AccountCreationFailedEvent for customer: {}, correlation: {}, reason: {}",
                    event.getCustomerNo(), event.getCorrelationId(), event.getReason());

        } catch (InterruptedException e) {
            logger.error("Interrupted while sending AccountCreationFailedEvent for customer: {}",
                    event.getCustomerNo(), e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException | RuntimeException e) {
            logger.error("Failed to send AccountCreationFailedEvent for customer: {}",
                    event.getCustomerNo(), e);
        }
    }
}
