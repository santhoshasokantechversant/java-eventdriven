/**
 * @file CountryRepository.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date September 02,2025
 * @version 1.0
 * @description Dao for country entity
 */

package com.techversant.customer_service.repository;

import com.techversant.customer_service.model.Country;
import com.techversant.customer_service.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    /**
     * Retrieves a list of {@link Country} entities associated with the given {@link Region}, ordered by country name in ascending order.
     *
     * @param region the {@link Region} entity for which to retrieve associated countries
     * @return a list of {@link Country} entities sorted by name in ascending order
     */
    List<Country> findByRegionOrderByNameAsc(Region region);
}
