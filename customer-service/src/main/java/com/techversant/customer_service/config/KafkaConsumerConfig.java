/**
 * @file KafkaConsumerConfig.java
 * @company Techversant Infotech
 * @author Siya Elsa Sabu
 * @version 1.0
 * @description Configuration class for Kafka consumer.
 */

package com.techversant.customer_service.config;

import com.techversant.common_lib.events.UserCreatedEvent;
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

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Configures a {@link ConsumerFactory} for consuming {@link UserCreatedEvent} messages from Kafka.
     * This factory is used to create Kafka consumers with JSON deserialization support.
     * Configuration details:
     * Bootstrap servers: specified by {@code bootstrapServers}.
     * Consumer group ID: "customer-service-group".
     * Key deserializer: {@link StringDeserializer}.
     * Value deserializer: {@link JsonDeserializer} for {@link UserCreatedEvent}.
     * Auto offset reset: "earliest" to consume messages from the beginning if no offset exists.
     * Trusted packages: "*" to allow all classes for deserialization.
     * Type info headers disabled: {@code USE_TYPE_INFO_HEADERS=false}.
     *
     * @return a {@link DefaultKafkaConsumerFactory} configured for {@link UserCreatedEvent} messages
     */
    @Bean
    public ConsumerFactory<String, UserCreatedEvent> userCreatedConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "customer-service-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // JSON Deserializer Configuration
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.techversant.common_lib.events.UserCreatedEvent");
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * Configures a {@link ConcurrentKafkaListenerContainerFactory} for consuming {@link UserCreatedEvent} messages.
     * This factory provides the listener container for Kafka consumers, enabling concurrent
     * message processing and integrating with the {@link #userCreatedConsumerFactory()}.
     * Key features:
     * Uses the {@link UserCreatedEvent} consumer factory for deserialization and group configuration.
     * Supports concurrent message listeners for scalable processing.
     *
     * @return a {@link ConcurrentKafkaListenerContainerFactory} configured for {@link UserCreatedEvent} messages
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserCreatedEvent> userCreatedListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userCreatedConsumerFactory());
        return factory;
    }
}