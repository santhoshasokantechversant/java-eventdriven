/**
 * @file WebClientConfig.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description WebClientConfiguration class
 */

package com.techversant.customer_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class WebClientConfig {

    /**
     * Configures a {@link WebClient} bean for making HTTP requests.
     * This allows the application to perform non-blocking, reactive HTTP calls
     * using Spring's WebClient. The builder can be customized elsewhere if needed.
     *
     * @param builder the {@link WebClient.Builder} injected by Spring
     * @return a fully built {@link WebClient} instance
     */
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
