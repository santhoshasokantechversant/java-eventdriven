/**
 * @file CustomerUserRepository.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description Dao for CustomerUser entity
 */

package com.techversant.customer_service.repository;

import com.techversant.customer_service.model.CustomerUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerUserRepository extends JpaRepository<CustomerUserEntity, UUID> {

    /**
     * Retrieves the CustomerUserEntity associated with the given customer ID.
     *
     * @param customerId the UUID of the customer
     * @return the matching CustomerUserEntity, or null if not found
     */
    CustomerUserEntity findByCustomerId(UUID customerId);
}
