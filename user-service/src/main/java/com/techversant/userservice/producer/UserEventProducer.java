package com.techversant.userservice.producer;

import org.springframework.stereotype.Component;
import com.techversant.common_lib.events.UserCreatedEvent;
import com.techversant.common_lib.events.UserCreationFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(UserEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.user-created}")
    private String userCreatedTopic;

    @Value("${app.kafka.topic.user-creation-failed}")
    private String userCreationFailedTopic;

    public UserEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendUserCreatedEvent(UserCreatedEvent event) {
        try {
            kafkaTemplate.send(userCreatedTopic, event);
            logger.info("Sent UserCreatedEvent for user: {}", event.getId());
        } catch (RuntimeException e) {
            logger.error("Failed to send UserCreatedEvent: {}", e.getMessage());
        }
    }

    public void sendUserCreationFailedEvent(UserCreationFailedEvent event) {
        try {
            kafkaTemplate.send(userCreationFailedTopic, event);
            logger.info("Sent UserCreationFailedEvent for customer: {}, reason: {}",
                    event.getEventId(), event.getReason());
        } catch (RuntimeException e) {
            logger.error("Failed to send UserCreationFailedEvent: {}", e.getMessage());
        }
    }

}
