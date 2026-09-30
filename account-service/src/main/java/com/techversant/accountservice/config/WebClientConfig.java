/**
 * @file WebClientConfig.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description WebClient config file
 */

package com.techversant.accountservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class WebClientConfig {

    /**
     * Creates a {@link WebClient} bean for making HTTP requests.
     * This method uses a {@link WebClient.Builder} to build a default {@link WebClient} instance
     * that can be injected wherever HTTP calls are needed in the application.
     *
     * @param builder the {@link WebClient.Builder} provided by Spring for customizing the client
     * @return a fully built {@link WebClient} instance
     */
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
