/**
 * @file AccountRollbackConsumer.java
 * @company Techversant Infotech
 * @author Siya Elsa Sabu
 * @version 1.0
 * @description Kafka rollback class
 */

package com.techversant.accountservice.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techversant.accountservice.service.impl.AccountService;
import com.techversant.common_lib.events.AccountRollbackEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AccountRollbackConsumer {

    private final AccountService accountService;
    private final ObjectMapper objectMapper;

    public AccountRollbackConsumer(AccountService accountService, ObjectMapper objectMapper) {
        this.accountService = accountService;
        this.objectMapper = objectMapper;
    }

    private static final Logger logger = LoggerFactory.getLogger(AccountRollbackConsumer.class);

    /**
     * Consumes "account-rollback" events from Kafka and performs account rollback operations.
     * This Kafka listener method:
     * - Listens to the "account-rollback" topic with the "account-service-group" consumer group.
     * - Receives the raw JSON message as a String.
     * - Converts the JSON string into an AccountRollbackEvent object using ObjectMapper.
     * - Invokes accountService to rollback account changes for the specified customer number.
     * - Logs all key steps including raw message, processing start, and successful rollback.
     * - Catches and logs any exceptions that occur during deserialization or rollback processing.
     *
     * @param message the raw JSON message received from Kafka representing an AccountRollbackEvent
     */
    @KafkaListener(topics = "account-rollback", groupId = "account-service-group")
    public void consumeAccountRollback(String message) {
        try {
            logger.info("🎯 ACCOUNT ROLLBACK RAW MESSAGE: {}", message);

            // Manually convert JSON string to object
            AccountRollbackEvent event = objectMapper.readValue(message, AccountRollbackEvent.class);

            logger.info("🔄 PROCESSING ACCOUNT ROLLBACK FOR CUSTOMER: {}", event.getCustomerNo());

            // Your rollback logic
            accountService.rollbackAccountByCustomerNo(event.getCustomerNo());

            logger.info("✅ ACCOUNT ROLLBACK COMPLETED FOR CUSTOMER: {}", event.getCustomerNo());

        } catch (JsonProcessingException | RuntimeException e) {
            logger.error("❌ ACCOUNT ROLLBACK PROCESSING FAILED: {}", e.getMessage(), e);
        }
    }
}

