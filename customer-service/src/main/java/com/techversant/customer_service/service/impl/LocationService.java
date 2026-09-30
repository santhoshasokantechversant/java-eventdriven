/**
 * @file LocationService.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date September 02,2025
 * @version 1.0
 * @description Class implementing methods of ILocationService interface
 */

package com.techversant.customer_service.service.impl;

import com.techversant.customer_service.dto.CityResponseDto;
import com.techversant.customer_service.dto.CountryResponseDto;
import com.techversant.customer_service.dto.StateResponseDto;
import com.techversant.customer_service.mapper.LocationMapper;
import com.techversant.customer_service.model.Country;
import com.techversant.customer_service.model.Region;
import com.techversant.customer_service.model.State;
import com.techversant.customer_service.repository.CityRepository;
import com.techversant.customer_service.repository.CountryRepository;
import com.techversant.customer_service.repository.RegionRepository;
import com.techversant.customer_service.repository.StateRepository;
import com.techversant.customer_service.service.ILocationService;
import com.techversant.customer_service.utils.exceptions.CountryNotFoundException;
import com.techversant.customer_service.utils.exceptions.RegionNotFoundException;
import com.techversant.customer_service.utils.exceptions.StateNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.techversant.customer_service.utils.Constants.*;

@Service
public class LocationService implements ILocationService {

    private final RegionRepository regionRepository;
    private final CountryRepository countryRepository;
    private final LocationMapper locationMapper;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;

    public LocationService(RegionRepository regionRepository, CountryRepository countryRepository, LocationMapper locationMapper, StateRepository stateRepository, CityRepository cityRepository) {
        this.regionRepository = regionRepository;
        this.countryRepository = countryRepository;
        this.locationMapper = locationMapper;
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
    }

    /**
     * Retrieves a list of {@link CountryResponseDto} objects associated with the specified region ID.
     * Throws a {@link RegionNotFoundException} if the region is not found in the database.
     *
     * @param regionId the ID of the region for which to retrieve countries
     * @return a list of {@link CountryResponseDto} containing country details belonging to the specified region
     * @throws RegionNotFoundException if no region is found with the given ID
     */
    @Override
    public List<CountryResponseDto> getCountriesForRegion(long regionId) {
        Region region = regionRepository.findById(regionId).orElse(null);
        if (region == null) {
            throw new RegionNotFoundException(REGION_NOT_FOUND);
        }
        return locationMapper.countryToCountryResponseDto(countryRepository.findByRegionOrderByNameAsc(region));
    }

    /**
     * Retrieves a list of {@link StateResponseDto} objects associated with the specified country ID.
     * Throws a {@link CountryNotFoundException} if the country is not found in the database.
     *
     * @param countryId the ID of the country for which to retrieve states
     * @return a list of {@link StateResponseDto} containing state details belonging to the specified country
     * @throws CountryNotFoundException if no country is found with the given ID
     */
    @Override
    public List<StateResponseDto> getStatesForCountry(long countryId) {
        Country country = countryRepository.findById(countryId).orElse(null);
        if (country == null) {
            throw new CountryNotFoundException(COUNTRY_NOT_FOUND);
        }
        return locationMapper.stateToStateResponseDto(stateRepository.findByCountryOrderByNameAsc(country));
    }

    /**
     * Retrieves a list of {@link CityResponseDto} objects associated with the specified state ID.
     * Throws a {@link StateNotFoundException} if the state is not found in the database.
     *
     * @param stateId the ID of the state for which to retrieve cities
     * @return a list of {@link CityResponseDto} containing city details belonging to the specified state
     * @throws StateNotFoundException if no state is found with the given ID
     */
    @Override
    public List<CityResponseDto> getCityForState(long stateId) {
        State state = stateRepository.findById(stateId).orElse(null);
        if (state == null) {
            throw new StateNotFoundException(STATE_NOT_FOUND);
        }
        return locationMapper.cityToCityResponseDto(cityRepository.findByStateOrderByNameAsc(state));
    }
}
