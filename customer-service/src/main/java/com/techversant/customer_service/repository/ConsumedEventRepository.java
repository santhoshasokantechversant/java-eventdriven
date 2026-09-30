/**
 * @file ConsumedEventRepository.java
 * @company Techversant Infotech
 * @author Siya Elsa Sabu
 * @version 1.0
 * @description Dao for ConsumedEvent entity
 */

package com.techversant.customer_service.repository;


import com.techversant.customer_service.model.ConsumedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumedEventRepository extends JpaRepository<ConsumedEvent, String> {

    /**
     * Checks if a record exists with the given event ID.
     * @param eventId the event ID to check
     * @return true if a record with the given event ID exists, false otherwise
     */
    boolean existsByEventId(String eventId);
}
