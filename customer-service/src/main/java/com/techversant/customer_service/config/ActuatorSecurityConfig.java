/**
 * @file ActuatorSecurityConfig.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 27, 2025
 * @version 1.0
 * @description Security configuration for Actuator endpoints (health/info)
 */

package com.techversant.customer_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ActuatorSecurityConfig {

    /**
     * Configures a separate {@link SecurityFilterChain} for Spring Boot Actuator endpoints.
     * This security configuration is applied only to requests under /actuator/**.
     * Key features:
     * ensures this filter chain runs before the main application security configuration.
     * All actuator endpoints are permitted without authentication.
     * CSRF protection is disabled for actuator endpoints since they are typically read-only or secured separately.
     * This allows actuator endpoints to be accessed safely without interfering with the main application's security rules.
     *
     * @param http the {@link HttpSecurity} object used to configure web-based security for actuator endpoints
     * @return a {@link SecurityFilterChain} configured specifically for actuator endpoints
     * @throws Exception if an error occurs while building the security filter chain
     */
    @Bean
    @Order(1) // ✅ Runs before main security config
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
