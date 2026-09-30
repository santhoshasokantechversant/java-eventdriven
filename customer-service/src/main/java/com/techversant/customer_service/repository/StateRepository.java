/**
 * @file StateRepository.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date September 02,2025
 * @version 1.0
 * @description Dao for state entity
 */

package com.techversant.customer_service.repository;

import com.techversant.customer_service.model.Country;
import com.techversant.customer_service.model.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {

    /**
     * Retrieves a list of {@link State} entities associated with the given {@link Country}, ordered by state name in ascending order.
     *
     * @param country the {@link Country} entity for which to retrieve associated states
     * @return a list of {@link State} entities sorted by name in ascending order
     */
    List<State> findByCountryOrderByNameAsc(Country country);
}
