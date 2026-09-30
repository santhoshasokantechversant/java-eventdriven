package com.techversant.userservice.consumer;

import com.techversant.common_lib.events.*;
import com.techversant.userservice.model.User;
import com.techversant.userservice.repository.UserRepository;
import com.techversant.userservice.service.impl.UserService;
import com.techversant.userservice.service.keyclock.KeyclockUserService;
import com.techversant.userservice.utils.exceptions.RollbackFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserRollbackConsumer {
    private final UserService userService;
    private final UserRepository userRepository;
    private final KeyclockUserService keyclockUserService;

    private static final Logger logger = LoggerFactory.getLogger(UserRollbackConsumer.class);

    private final Map<Long, String> customerEmailMap = new ConcurrentHashMap<>();

    public UserRollbackConsumer(UserService userService,
                                UserRepository userRepository,
                                KeyclockUserService keyclockUserService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.keyclockUserService = keyclockUserService;
    }

    /**
     * Track customer email for rollback
     */
    public void trackCustomerEmail(Long customerNo, String email) {
        customerEmailMap.put(customerNo, email);
        logger.info("📝 Tracked customer {} -> email {}", customerNo, email);
    }

    /**
     * Remove customer mapping
     */
    public void removeCustomerMapping(Long customerNo) {
        customerEmailMap.remove(customerNo);
        logger.info("🧹 Removed mapping for customer: {}", customerNo);
    }

    /**
     * Get email by customer number
     */
    public String getEmailByCustomerNo(Long customerNo) {
        return customerEmailMap.get(customerNo);
    }

    @KafkaListener(
            topics = "customer.rollback",
            groupId = "user-service-rollback-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Retryable(
            retryFor = {Exception.class, RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    @Transactional
    public void consumeCustomerRollback(UserRollbackEvent event) {
        logger.info("🎯 CUSTOMER ROLLBACK EVENT RECEIVED IN USER SERVICE - Email: {}, Reason: {}, EventId: {}",
                event.getEmail(), event.getReason(), event.getEventId());

        try {
            // Idempotency check - verify user exists before attempting rollback
            if (event.getEmail() != null && userService.userExistsByEmail(event.getEmail())) {
                logger.info("🔄 ATTEMPTING ROLLBACK FOR EMAIL: {}", event.getEmail());

                // 1. Remove from Keycloak first
                removeUserFromKeycloakByEmail(event.getEmail());

                // 2. Then remove from local database
                userService.rollbackUserByEmail(event.getEmail());

                logger.info("✅ ROLLBACK COMPLETED FOR EMAIL: {}", event.getEmail());
            } else {
                logger.warn("⚠️ USER NOT FOUND FOR ROLLBACK - Email: {}, or already rolled back", event.getEmail());
            }
        }catch (RuntimeException e) {
            throw new RollbackFailureException("Rollback failed for email: " + event.getEmail(), e);
        }
    }

    // Fallback method when all retries exhausted
    @Recover
    public void consumeCustomerRollbackFallback(Exception e, UserRollbackEvent event) {
        logger.error("🚨 ALL ROLLBACK RETRY ATTEMPTS FAILED - Email: {}, EventId: {}, Error: {}",
                event.getEmail(), event.getEventId(), e.getMessage());
    }

    /**
     * NEW METHOD: Finds the user by email and deletes them from Keycloak using the existing service.
     */
    private void removeUserFromKeycloakByEmail(String email) {
        try {
            logger.info("🗑️ ATTEMPTING TO REMOVE USER FROM KEYCLOAK BY EMAIL: {}", email);

            // 1. Find the user in your local database by email
            User user = userRepository.findByEmail(email); // Ensure this method exists in your UserRepository
            if (user == null) {
                logger.warn("ℹ️ User with email '{}' not found in local database. Cannot proceed with Keycloak deletion.", email);
                return;
            }

            // 2. Get the Keycloak User ID stored in your user entity
            String keycloakUserId = user.getKeyclockUserId(); // Ensure this field name matches your User entity

            if (keycloakUserId != null) {
                // 3. Use your existing, proven service to delete the user from Keycloak
                keyclockUserService.deleteUser(keycloakUserId);
                logger.info("✅ SUCCESSFULLY REMOVED USER FROM KEYCLOAK. Email: {}, Keycloak ID: {}", email, keycloakUserId);
            } else {
                logger.warn("⚠️ Keycloak User ID is null for email: {}. Cannot delete from Keycloak.", email);
            }

        } catch (RuntimeException e) {
            logger.error("❌ FAILED TO REMOVE USER FROM KEYCLOAK: {}", e.getMessage(), e);
            // Decide if you need to throw the exception based on your rollback strategy
            // A failed Keycloak deletion might require manual intervention
        }
    }

    @KafkaListener(
            topics = "account.creation.failed",
            groupId = "user-service-compensation",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAccountFailure(AccountCreationFailedEvent event) {
        logger.info("🎯 ACCOUNT FAILURE EVENT RECEIVED IN USER SERVICE - Customer: {}, Reason: {}",
                event.getCustomerNo(), event.getReason());

        try {
            String email = getEmailByCustomerNo(event.getCustomerNo());
            if (email != null) {
                logger.info("🔄 ATTEMPTING ROLLBACK FOR EMAIL: {}", email);

                removeUserFromKeycloakByEmail(email);
                userService.rollbackUserByEmail(email);

                logger.info("✅ ROLLBACK COMPLETED FOR EMAIL: {}", email);
            } else {
                logger.error("❌ NO EMAIL MAPPING FOUND FOR CUSTOMER: {}", event.getCustomerNo());
            }
        } catch (RuntimeException e) {
            logger.error("❌ ROLLBACK FAILED: {}", e.getMessage(), e);
        }
    }
}
