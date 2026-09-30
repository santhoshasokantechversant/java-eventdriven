/**
 * @file LocationMapper.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date September 02,2025
 * @version 1.0
 * @description Mapper class to map different entity classes associated with location to different Dto classes
 */

package com.techversant.customer_service.mapper;

import com.techversant.customer_service.dto.CityResponseDto;
import com.techversant.customer_service.dto.CountryResponseDto;
import com.techversant.customer_service.dto.StateResponseDto;
import com.techversant.customer_service.model.City;
import com.techversant.customer_service.model.Country;
import com.techversant.customer_service.model.State;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocationMapper {

    /**
     * Converts a list of {@link Country} entities to a list of {@link CountryResponseDto} objects.
     *
     * @param countries the list of {@link Country} entities to be converted
     * @return a list of {@link CountryResponseDto} containing country IDs and names
     */
    public List<CountryResponseDto> countryToCountryResponseDto(List<Country> countries) {
        return countries.stream().map(e -> new CountryResponseDto(e.getId(), e.getName())).toList();
    }

    /**
     * Converts a list of {@link State} entities to a list of {@link StateResponseDto} objects.
     *
     * @param states the list of {@link State} entities to be converted
     * @return a list of {@link StateResponseDto} containing state IDs and names
     */
    public List<StateResponseDto> stateToStateResponseDto(List<State> states) {
        return states.stream().map(e -> new StateResponseDto(e.getId(), e.getName())).toList();
    }

    /**
     * Converts a list of {@link City} entities to a list of {@link CityResponseDto} objects.
     *
     * @param cities the list of {@link City} entities to be converted
     * @return a list of {@link CityResponseDto} containing city IDs and names
     */
    public List<CityResponseDto> cityToCityResponseDto(List<City> cities) {
        return cities.stream().map(e -> new CityResponseDto(e.getId(), e.getName())).toList();
    }
}