/**
 * @file CustomerEventConsumer.java
 * @company Techversant Infotech
 * @author Siya Elsa Sabu
 * @version 1.0
 * @description This component listens to user-created Kafka events and updates the corresponding customer-user records.
 */

package com.techversant.customer_service.consumer;

import com.techversant.common_lib.events.UserCreatedEvent;
import com.techversant.customer_service.model.CustomerUserEntity;
import com.techversant.customer_service.repository.CustomerUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CustomerEventConsumer {

    private final CustomerUserRepository customerUserRepository;

    public CustomerEventConsumer(CustomerUserRepository customerUserRepository) {
        this.customerUserRepository = customerUserRepository;
    }

    private static final Logger logger = LoggerFactory.getLogger(CustomerEventConsumer.class);

    /**
     * Kafka listener for the "user.created" topic.
     * This method consumes {@link UserCreatedEvent} messages and updates the
     * corresponding customer-user mapping in the database.
     * Steps performed:
     * Logs the incoming event for auditing and debugging.
     * Creates a new {@link CustomerUserEntity} and sets customer ID, user ID, and username from the event.
     * Saves the entity using {@link CustomerUserRepository}.
     * Logs successful completion or catches and logs any exceptions.
     *
     * @param event the {@link UserCreatedEvent} containing customer and user details
     */
    @KafkaListener(topics = "user.created", groupId = "customer-service-group", containerFactory = "userCreatedListenerFactory")
    public void consumeUserCreated(UserCreatedEvent event) {
        try {
            logger.info("Updating customer {} with user ID: {}", event.getCustomerId(), event.getUserId());
            CustomerUserEntity customerUserEntity = new CustomerUserEntity();
            customerUserEntity.setCustomerId(event.getCustomerId());
            customerUserEntity.setUserId(event.getUserId());
            customerUserEntity.setUserName(event.getUserName());
            this.customerUserRepository.save(customerUserEntity);
            logger.info("Customer {} updated with user ID: {}", event.getCustomerId(), event.getUserId());

        } catch (RuntimeException e) {
            logger.error("Failed to update customer with user ID: {}", e.getMessage());
        }
    }
}
