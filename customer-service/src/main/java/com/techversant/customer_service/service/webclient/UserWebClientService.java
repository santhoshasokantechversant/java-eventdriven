/**
 * @file UserWebClientService.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description Service class for interacting with the User service using WebClient to send customer data and validate user existence.
 */

package com.techversant.customer_service.service.webclient;


import com.techversant.common_lib.events.UserDetailsDto;
import com.techversant.customer_service.dto.ApiResponse;
import com.techversant.customer_service.dto.CustomerDto;
import com.techversant.customer_service.utils.exceptions.SomethingWentWrongException;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class UserWebClientService {

    @Value("${user.service.url}")
    private String userServiceUrl;

    private final WebClient webClient;
    private static final String STATUS_SUCCESS = "success";

    public UserWebClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Sends customer data to the User Service to check for existing users.
     * This method sends an asynchronous POST request containing the customer's
     * email and phone number to the User Service. It validates whether a user
     * with the same details already exists.
     *
     * @param customerDTO the CustomerDto containing customer details to check
     * @param token       the JWT token used to authorize the request
     * @return a Mono emitting true if the user does not exist, or an error if the
     * user already exists or the request fails
     */
    public Mono<Boolean> sendCustomerDataToUserService(CustomerDto customerDTO, String token) {
        UserDetailsDto userDetailsDto = new UserDetailsDto();
        userDetailsDto.setEmail(customerDTO.getEmail());
        userDetailsDto.setPhoneNumber(customerDTO.getPhoneNumber());

        return webClient.post()
                .uri(userServiceUrl + "/api/v1/users/check-exist-customer")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(userDetailsDto)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .flatMap(response -> {
                    if (STATUS_SUCCESS.equalsIgnoreCase(response.getStatus())) {
                        return Mono.just(true);
                    } else {
                        return Mono.error(new SomethingWentWrongException(response.getMessage()));
                    }
                })
                .onErrorMap(ex -> new SomethingWentWrongException("User validation failed: " + ex.getMessage(), ex));
    }

}

