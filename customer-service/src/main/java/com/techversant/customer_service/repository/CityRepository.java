/**
 * @file CityRepository.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date September 02,2025
 * @version 1.0
 * @description Dao for city entity
 */

package com.techversant.customer_service.repository;

import com.techversant.customer_service.model.City;
import com.techversant.customer_service.model.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    /**
     * Retrieves a list of {@link City} entities associated with the given {@link State}, ordered by city name in ascending order.
     *
     * @param state the {@link State} entity for which to retrieve associated cities
     * @return a list of {@link City} entities sorted by name in ascending order
     */
    List<City> findByStateOrderByNameAsc(State state);
}
