/**
 * @file ILocationService.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date september 02,2025
 * @version 1.0
 * @description Service interface for handling operations related to different location entities
 */

package com.techversant.customer_service.service;

import com.techversant.customer_service.dto.CityResponseDto;
import com.techversant.customer_service.dto.CountryResponseDto;
import com.techversant.customer_service.dto.StateResponseDto;

import java.util.List;

public interface ILocationService {

    /**
     * Retrieves a list of {@link CountryResponseDto} objects for the specified region ID.
     *
     * @param regionId the ID of the region for which to retrieve countries
     * @return a list of {@link CountryResponseDto} containing country details belonging to the specified region
     */
    List<CountryResponseDto> getCountriesForRegion(long regionId);

    /**
     * Retrieves a list of {@link StateResponseDto} objects for the specified country ID.
     *
     * @param countryId the ID of the country for which to retrieve states
     * @return a list of {@link StateResponseDto} containing state details belonging to the specified country
     */
    List<StateResponseDto> getStatesForCountry(long countryId);

    /**
     * Retrieves a list of {@link CityResponseDto} objects for the specified state ID.
     *
     * @param stateId the ID of the state for which to retrieve cities
     * @return a list of {@link CityResponseDto} containing city details belonging to the specified state
     */
    List<CityResponseDto> getCityForState(long stateId);
}
