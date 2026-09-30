/**
 * @file FeignConfig.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description This configuration class provides a custom Feign encoder for handling form data.
 */

package com.techversant.customer_service.config;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    /**
     * Configures a Feign {@link Encoder} that supports form-encoded data.
     * This encoder allows Feign clients to send requests with
     * application/x-www-form-urlencode content type, using Spring's
     * {@link SpringFormEncoder} for integration with Spring MVC form parameters.
     *
     * @return a {@link SpringFormEncoder} instance for form-encoded requests
     */
    @Bean
    public Encoder feignFormEncoder() {
        return new SpringFormEncoder();
    }
}