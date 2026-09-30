/**
 * @file LocationController.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date September 02,2025
 * @version 1.0
 * @description Controller to handle all apis related to location
 */

package com.techversant.customer_service.controller;

import com.techversant.customer_service.dto.ApiResponse;
import com.techversant.customer_service.dto.CityResponseDto;
import com.techversant.customer_service.dto.CountryResponseDto;
import com.techversant.customer_service.dto.StateResponseDto;
import com.techversant.customer_service.service.ILocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.techversant.customer_service.utils.Constants.*;

@RestController
@RequestMapping("/api/v1/customers/locations")
public class LocationController {

    private final ILocationService iLocationService;

    public LocationController(ILocationService iLocationService) {
        this.iLocationService = iLocationService;
    }

    /**
     * Retrieves a list of countries associated with the specified region ID.
     * Returns a {@code ResponseEntity} with a custom {@code ApiResponse} containing the country data and a {@code 200 OK} HTTP status code.
     *
     * @param regionId the ID of the region for which to fetch the list of countries; defaults to {@code 3} if not provided
     * @return a {@link ResponseEntity} containing an {@code ApiResponse} with the list of countries, a success message, and HTTP 200 status code
     */
    @GetMapping(path = "/get-countries-for-region")
    public ResponseEntity<ApiResponse<List<CountryResponseDto>>> getCountriesForRegion(@RequestParam(defaultValue = "3") long regionId) {
        List<CountryResponseDto> countries = iLocationService.getCountriesForRegion(regionId);
        ApiResponse<List<CountryResponseDto>> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setData(countries);
        apiResponse.setMessage(COUNTRIES_RETRIEVED);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Retrieves a list of states associated with the specified country ID.
     * Returns a {@code ResponseEntity} with a custom {@code ApiResponse} containing the state data and a {@code 200 OK} HTTP status code.
     *
     * @param countryId the ID of the country for which to fetch the list of states
     * @return a {@link ResponseEntity} containing an {@code ApiResponse} with the list of states, a success message, and HTTP 200 status code
     */
    @GetMapping(path = "/get-states-for-country/{countryId}")
    public ResponseEntity<ApiResponse<List<StateResponseDto>>> getStatesForCountry(@PathVariable long countryId) {
        List<StateResponseDto> states = iLocationService.getStatesForCountry(countryId);
        ApiResponse<List<StateResponseDto>> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(STATES_RETRIEVED);
        apiResponse.setData(states);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Retrieves a list of cities associated with the specified state ID.
     * Returns a {@code ResponseEntity} with a custom {@code ApiResponse} containing the city data and a {@code 200 OK} HTTP status code.
     *
     * @param stateId the ID of the state for which to fetch the list of cities
     * @return a {@link ResponseEntity} containing an {@code ApiResponse} with the list of cities, a success message, and HTTP 200 status code
     */
    @GetMapping(path = "/get-city-for-state/{stateId}")
    public ResponseEntity<ApiResponse<List<CityResponseDto>>> getCityForState(@PathVariable long stateId) {
        List<CityResponseDto> cities = iLocationService.getCityForState(stateId);
        ApiResponse<List<CityResponseDto>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage(CITIES_RETRIEVED);
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setData(cities);
        return ResponseEntity.ok(apiResponse);
    }
}
