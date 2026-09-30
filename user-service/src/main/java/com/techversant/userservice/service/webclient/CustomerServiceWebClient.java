package com.techversant.userservice.service.webclient;

import com.techversant.common_lib.events.CustomerReturnDto;
import com.techversant.userservice.dto.ApiResponse;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class CustomerServiceWebClient {
    private final WebClient webClient;

    @Value("${service.url}")
    private String userServiceUrl;

    // Use WebClient.Builder, not WebClient directly
    public CustomerServiceWebClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<ApiResponse<CustomerReturnDto>> getCustomerData(String token, UUID id) {
        return webClient.get()
                .uri(userServiceUrl + "/api/v1/customers/delete-customer-by-user-id/" + id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<CustomerReturnDto>>() {});
    }

}
