/**
 * @file KafkaErrorHandlerConfig.java
 * @company Techversant Infotech
 * @author Shajahan M
 * @version 1.0
 * @description Configuration file for kafka error handling
 */

package com.techversant.accountservice.config;


import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlerConfig {


    private final Logger logger = LoggerFactory.getLogger(KafkaErrorHandlerConfig.class);

    /**
     * Creates a {@link DefaultErrorHandler} bean for handling errors during Kafka message consumption.
     * This error handler retries failed messages a fixed number of times with a delay,
     * and publishes messages that still fail after retries to a Dead Letter Topic (DLT).
     * Key features:
     * Retries failed messages 3 times with a 5-second delay.
     * Uses {@link DeadLetterPublishingRecoverer} to send failed messages to <original-topic>.DLT.
     * Logs every retry attempt with the message value and exception details.
     *
     * @param kafkaTemplate the {@link KafkaTemplate} used to publish failed messages to the DLT
     * @return a configured {@link DefaultErrorHandler} for Kafka listeners
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {

        // DeadLetterPublishingRecoverer → publishes to <original-topic>.DLT
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (consumerRecord, exception) ->
                        // send failed messages to a ".DLT" topic
                        new TopicPartition(consumerRecord.topic() + ".DLT", consumerRecord.partition())
        );

        // Retry 3 times with 5s delay before sending to DLT
        FixedBackOff fixedBackOff = new FixedBackOff(5000L, 3);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, fixedBackOff);

        // Log the error
        errorHandler.setRetryListeners((consumerRecord, ex, deliveryAttempt) ->
                logger.error("Retry attempt {} for record: {} due to {}", deliveryAttempt, consumerRecord.value(), ex.getMessage(), ex)
        );

        return errorHandler;
    }
}
