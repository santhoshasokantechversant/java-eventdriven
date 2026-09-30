/**
 * @file AccountsWebclientService.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description Service class for interacting with the Accounts service using WebClient to fetch account data by customer ID.
 */

package com.techversant.customer_service.service.webclient;

import com.techversant.common_lib.events.AccountsReturnDto;
import com.techversant.customer_service.dto.ApiResponse;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class AccountsWebclientService {

    @Value("${user.service.url}")
    private String userServiceUrl;

    private final WebClient webClient;

    public AccountsWebclientService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Retrieves account data for a given customer ID from the Accounts Service.
     * This method sends an asynchronous GET request to the Accounts Service
     * using a provided JWT token for authorization. The response is returned
     * as a Mono wrapping an ApiResponse containing AccountsReturnDto.
     *
     * @param token the JWT token to authorize the request
     * @param id the UUID of the customer whose account data is being requested
     * @return a Mono emitting an ApiResponse containing AccountsReturnDto
     */
    public Mono<ApiResponse<AccountsReturnDto>> getAccountData(String token, UUID id) {
        return webClient.get()
                .uri(userServiceUrl + "/api/v1/account/get-account-by-customer-id/" + id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<AccountsReturnDto>>() {
                });
    }

}
