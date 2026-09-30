/**
 * @file CustomerWebClientService.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description Class interacting with user-service to fetch details of the user.
 */

package com.techversant.accountservice.service.webclient;

import com.techversant.accountservice.dto.ApiResponse;
import com.techversant.accountservice.dto.CustomerDto;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class CustomerWebClientService {

    private final WebClient webClient;

    @Value("${service.url}")
    private String userServiceUrl;

    private static final String STATUS_SUCCESS = "success";

    // Use WebClient.Builder, not WebClient directly
    public CustomerWebClientService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Retrieves the total number of users from the user service.
     * This method sends a GET request to the user service endpoint with the provided JWT token
     * for authentication. It expects an ApiResponse containing the user count. If the request
     * fails or the response status is not "success", it returns 0.
     *
     * @param token the JWT token used for authorization with the user service
     * @return a Mono emitting the number of users if successful, or 0 in case of failure
     */
    public Mono<Integer> getUserCount(String token) {
        return webClient.get()
                .uri(userServiceUrl + "/api/v1/customers/users-count")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .map(response -> STATUS_SUCCESS.equalsIgnoreCase(response.getStatus())
                        ? response.getCount()
                        : 0)
                .onErrorReturn(0);
    }

    /**
     * Retrieves customer data from the user service for a given user ID.
     * Sends a GET request to the user service endpoint using the provided JWT token
     * for authorization. Returns a Mono wrapping an ApiResponse containing CustomerDto.
     * In case of any error during the request, it returns a default ApiResponse with
     * status "FAIL" and null data.
     *
     * @param token the JWT token used for authorization with the user service
     * @param id    the UUID of the customer whose data is being requested
     * @return a Mono emitting an ApiResponse containing CustomerDto if successful,
     * or a default ApiResponse with status "FAIL" in case of error
     */
    public Mono<ApiResponse<CustomerDto>> getUserData(String token, UUID id) {
        return webClient.get()
                .uri(userServiceUrl + "/api/v1/customers/get-customer-by-user-id/" + id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<CustomerDto>>() {
                })
                .onErrorResume(e -> {
                    // Return a default ApiResponse on error
                    ApiResponse<CustomerDto> errorResponse = new ApiResponse<>();
                    errorResponse.setStatus("FAIL");
                    errorResponse.setMessage(e.getMessage());
                    errorResponse.setCount(0);
                    errorResponse.setData(null);
                    return Mono.just(errorResponse);
                });
    }

}


