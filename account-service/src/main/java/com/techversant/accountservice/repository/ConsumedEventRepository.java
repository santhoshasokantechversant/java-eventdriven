/**
 * @file ConsumedEventRepository.java
 * @company Techversant Infotech
 * @author Siya Elsa Sabu
 * @version 1.0
 * @description Repository interface for performing database operations related to consumed events.
 */

package com.techversant.accountservice.repository;

import com.techversant.accountservice.model.ConsumedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumedEventRepository extends JpaRepository<ConsumedEvent, String> {

    /**
     * Checks whether an event with the given event ID has already been processed.
     * This method queries the database to determine if an entity exists
     * with the specified {@code eventId}.
     *
     * @param eventId the unique identifier of the event
     * @return {@code true} if an entity with the given event ID exists, {@code false} otherwise
     */
    boolean existsByEventId(String eventId);
}
