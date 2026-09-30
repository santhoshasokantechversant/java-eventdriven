/**
 * @file KafkaConfig.java
 * @company Techversant Infotech
 * @author Siya Elsa Sabu
 * @version 1.0
 * @description Configuration class for Kafka.
 */

package com.techversant.customer_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    /**
     * Creates a Kafka topic named "customer.created".
     * This topic is configured for handling customer creation events.
     * Configuration details:
     * Number of partitions: 3 — allows parallel processing of messages.
     * Number of replicas: 1 — single copy for fault tolerance.
     *
     * @return a {@link NewTopic} instance representing the "customer.created" topic
     */
    @Bean
    public NewTopic customerCreatedTopic() {
        return TopicBuilder.name("customer.created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Creates a Kafka topic named "customer.updated".
     * This topic is configured for handling customer update events.
     * Configuration details:
     * Number of partitions: 3 — allows parallel processing of messages.
     * Number of replicas: 1 — single copy for fault tolerance.
     *
     * @return a {@link NewTopic} instance representing the "customer.updated" topic
     */
    @Bean
    public NewTopic customerUpdatedTopic() {
        return TopicBuilder.name("customer.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Creates a Kafka topic named "customer.deleted".
     * This topic is configured for handling customer deletion events.
     * Configuration details:
     * Number of partitions: 3 — allows parallel processing of messages.
     * Number of replicas: 1 — single copy for fault tolerance.
     *
     * @return a {@link NewTopic} instance representing the "customer.deleted" topic
     */
    @Bean
    public NewTopic customerDeletedTopic() {
        return TopicBuilder.name("customer.deleted")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Creates a Kafka topic named "customer.rollback".
     * This topic is configured for handling customer rollback events, typically used
     * to revert previous actions in case of failures or errors.
     * Configuration details:
     * Number of partitions: 3 — allows parallel processing of messages.
     * Number of replicas: 1 — single copy for fault tolerance.
     *
     * @return a {@link NewTopic} instance representing the "customer.rollback" topic
     */
    @Bean
    public NewTopic customerRollbackTopic() {
        return TopicBuilder.name("customer.rollback")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Creates a Kafka topic named "user.creation.failed".
     * This topic is configured for handling failed user creation events, allowing
     * failed operations to be processed or retried asynchronously.
     * Configuration details:
     * Number of partitions: 3 — allows parallel processing of messages.
     * Number of replicas: 1 — single copy for fault tolerance.
     *
     * @return a {@link NewTopic} instance representing the "user.creation.failed" topic
     */
    @Bean
    public NewTopic userCreationFailedTopic() {
        return TopicBuilder.name("user.creation.failed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Creates a Kafka topic named "account.creation.failed".
     * This topic is configured for handling failed account creation events, allowing
     * failed operations to be processed or retried asynchronously.
     * Configuration details:
     * Number of partitions: 3 — allows parallel processing of messages.
     * Number of replicas: 1 — single copy for fault tolerance.
     *
     * @return a {@link NewTopic} instance representing the "account.creation.failed" topic
     */
    @Bean
    public NewTopic accountCreationFailedTopic() {
        return TopicBuilder.name("account.creation.failed")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
