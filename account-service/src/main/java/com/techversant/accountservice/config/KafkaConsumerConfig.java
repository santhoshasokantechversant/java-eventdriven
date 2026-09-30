/**
 * @file KafkaConsumerConfig.java
 * @company Techversant Infotech
 * @author Siya Elsa Sabu
 * @version 1.0
 * @description Configuration file for kafka consumer
 */

package com.techversant.accountservice.config;

import com.techversant.accountservice.utils.Constants;
import com.techversant.common_lib.events.CustomerCreatedEvent;
import com.techversant.common_lib.events.CustomerDeletedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Creates a Kafka {@link ConsumerFactory} bean for consuming {@link CustomerCreatedEvent} messages.
     * This factory defines how Kafka consumers will be configured, including server connection,
     * group ID, deserialization, and message handling for CustomerCreatedEvent objects.
     *
     * @return a configured {@link ConsumerFactory} for CustomerCreatedEvent messages
     */
    @Bean
    public ConsumerFactory<String, CustomerCreatedEvent> customerCreatedConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.ACCOUNT_SERVICE_GROUP);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, Constants.EARLIEST);

        // JSON Deserializer Configuration
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.techversant.common_lib.events.CustomerCreatedEvent");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Creates a {@link ConcurrentKafkaListenerContainerFactory} bean for handling
     * {@link CustomerCreatedEvent} messages.
     * This factory is responsible for creating Kafka listener containers that
     * consume messages concurrently using the {@link ConsumerFactory} defined for
     * CustomerCreatedEvent.
     *
     * @return a configured {@link ConcurrentKafkaListenerContainerFactory} for CustomerCreatedEvent
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CustomerCreatedEvent> customerCreatedListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CustomerCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(customerCreatedConsumerFactory());
        return factory;
    }

    /**
     * Creates a Kafka {@link ConsumerFactory} bean for consuming plain String messages.
     * This factory configures Kafka consumers that handle both keys and values as strings,
     * typically used for simple message consumption without custom object deserialization.
     *
     * @return a configured {@link ConsumerFactory} for String key-value pairs
     */
    @Bean
    public ConsumerFactory<String, String> stringConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.ACCOUNT_SERVICE_GROUP);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, Constants.EARLIEST);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Creates a {@link ConcurrentKafkaListenerContainerFactory} bean for handling
     * Kafka messages with String key-value pairs.
     * This factory enables the creation of concurrent Kafka listener containers
     * that consume plain String messages using the configured {@link ConsumerFactory}.
     * It is typically used with {@link org.springframework.kafka.annotation.KafkaListener}
     * methods expecting String payloads.
     *
     * @return a configured {@link ConcurrentKafkaListenerContainerFactory} for String messages
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stringConsumerFactory());
        return factory;
    }

    /**
     * Creates a Kafka {@link ConsumerFactory} bean for consuming {@link CustomerDeletedEvent} messages.
     * This factory configures Kafka consumers to handle JSON payloads mapped to
     * {@link CustomerDeletedEvent} objects. It sets up deserialization, server connection,
     * group ID, and offset reset behavior.
     *
     * @return a configured {@link ConsumerFactory} for CustomerDeletedEvent messages
     */
    @Bean
    public ConsumerFactory<String, CustomerDeletedEvent> customerDeletedConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.ACCOUNT_SERVICE_GROUP);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, Constants.EARLIEST);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.techversant.common_lib.events.CustomerDeletedEvent");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Creates a {@link ConcurrentKafkaListenerContainerFactory} bean for handling
     * {@link CustomerDeletedEvent} messages.
     * This factory is responsible for creating Kafka listener containers that
     * consume {@link CustomerDeletedEvent} messages concurrently. It uses the
     * {@link #customerDeletedConsumerFactory()} to configure consumers, including
     * deserialization and group ID settings.
     * It is typically used with {@link org.springframework.kafka.annotation.KafkaListener}
     * methods that expect {@link CustomerDeletedEvent} payloads.
     *
     * @return a configured {@link ConcurrentKafkaListenerContainerFactory} for CustomerDeletedEvent
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CustomerDeletedEvent> customerDeletedListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CustomerDeletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(customerDeletedConsumerFactory());
        return factory;
    }
}