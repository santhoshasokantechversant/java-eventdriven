/**
 * @file CorsGlobalConfiguration.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description Global CORS configuration that defines allowed origins, headers, methods, and credentials for all incoming requests.
 */

package com.techversant.gatewayservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsGlobalConfiguration {

    /**
     * Configures a global CORS filter for the application.
     * This bean allows cross-origin requests from the configured origins
     * (app.cors.allowed-origins / CORS_ALLOWED_ORIGINS), enables credentials,
     * and permits common HTTP methods and headers for all endpoints ("/**").
     *
     * @param allowedOrigins frontend origins allowed to call the API
     * @return a CorsWebFilter that enforces the specified CORS configuration
     */
    @Bean
    public CorsWebFilter corsWebFilter(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins.stream().map(String::trim).filter(o -> !o.isEmpty()).toList());
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}

