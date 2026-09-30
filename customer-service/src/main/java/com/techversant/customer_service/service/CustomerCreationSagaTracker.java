/**
 * @file CustomerCreationSagaTracker.java
 * @company Techversant Infotech
 * @author Siya Elsa Sabu
 * @version 1.0
 * @description Service class that tracks and manages the completion status of customer creation sagas across multiple services.
 */

package com.techversant.customer_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class CustomerCreationSagaTracker {

    private final Map<String, CompletableFuture<Boolean>> sagaCompletions = new ConcurrentHashMap<>();
    private final Map<String, Boolean> userServiceResults = new ConcurrentHashMap<>();
    private final Map<String, Boolean> accountServiceResults = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(CustomerCreationSagaTracker.class);

    /**
     * Registers a new saga for customer creation or other distributed operations.
     * This method generates a unique correlation ID for the saga, stores a
     * corresponding CompletableFuture in the sagaCompletions map to track
     * its completion, and returns the correlation ID.
     *
     * @param customerNo the customer number associated with the saga
     * @return a unique correlation ID representing the registered saga
     */
    public String registerSaga(Long customerNo) {
        String correlationId = "saga_" + UUID.randomUUID().toString();
        sagaCompletions.put(correlationId, new CompletableFuture<>());
        return correlationId;
    }

    /**
     * Marks the User Service step of a saga as completed.
     * This method records the completion status of the User Service for the
     * given correlation ID and triggers a check to see if the entire saga
     * has been completed.
     *
     * @param correlationId the unique correlation ID of the saga
     * @param success       true if the User Service step succeeded, false otherwise
     */
    public void markUserServiceCompleted(String correlationId, boolean success) {
        userServiceResults.put(correlationId, success);
        checkSagaCompletion(correlationId);
    }

    /**
     * Marks the Account Service step of a saga as completed.
     * This method records the completion status of the Account Service for the
     * given correlation ID and triggers a check to see if the entire saga
     * has been completed.
     *
     * @param correlationId the unique correlation ID of the saga
     * @param success       true if the Account Service step succeeded, false otherwise
     */
    public void markAccountServiceCompleted(String correlationId, boolean success) {
        accountServiceResults.put(correlationId, success);
        checkSagaCompletion(correlationId);
    }

    /**
     * Checks if all steps of a saga are completed and completes the corresponding CompletableFuture.
     * This method verifies whether both the User Service and Account Service have reported
     * their completion status for the given correlation ID. If both results are available,
     * it calculates the overall success and completes the associated CompletableFuture.
     *
     * @param correlationId the unique correlation ID of the saga
     */
    private void checkSagaCompletion(String correlationId) {
        Boolean userResult = userServiceResults.get(correlationId);
        Boolean accountResult = accountServiceResults.get(correlationId);

        // Only complete when BOTH services have reported
        if (userResult != null && accountResult != null) {
            boolean overallSuccess = userResult && accountResult;
            CompletableFuture<Boolean> future = sagaCompletions.get(correlationId);
            if (future != null && !future.isDone()) {
                future.complete(overallSuccess);
            }
        }
    }

    /**
     * Waits for the completion of a saga with the specified correlation ID.
     * This method blocks up to the specified timeout for the saga to complete.
     * It retrieves the corresponding CompletableFuture from the sagaCompletions map
     * and waits for it to complete. Returns true if the saga succeeded, false if it
     * failed, timed out, or was not found.
     * After waiting, the saga data is cleaned up from internal tracking maps.
     *
     * @param correlationId  the unique correlation ID of the saga
     * @param timeoutSeconds the maximum time in seconds to wait for saga completion
     * @return true if the saga completed successfully, false otherwise
     */
    public boolean waitForSagaCompletion(String correlationId, long timeoutSeconds) {
        try {
            CompletableFuture<Boolean> future = sagaCompletions.get(correlationId);
            if (future == null) {
                return false;
            }

            Boolean result = future.get(timeoutSeconds, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(result);

        } catch (TimeoutException e) {
            logger.error("Saga timeout for correlation: {}", correlationId);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // preserve interrupt status
            logger.error("Saga wait interrupted for correlation: {}", correlationId, e);
            return false;
        } catch (ExecutionException | RuntimeException e) {
            logger.error("Saga wait failed for correlation: {}", correlationId, e);
            return false;
        } finally {
            cleanupSaga(correlationId);
        }
    }

    /**
     * Cleans up all tracking data associated with a saga.
     * This method removes the correlation ID from the internal maps used to track
     * saga completion and service results, freeing resources and preventing memory leaks.
     *
     * @param correlationId the unique correlation ID of the saga to clean up
     */
    public void cleanupSaga(String correlationId) {
        sagaCompletions.remove(correlationId);
        userServiceResults.remove(correlationId);
        accountServiceResults.remove(correlationId);
    }
}
