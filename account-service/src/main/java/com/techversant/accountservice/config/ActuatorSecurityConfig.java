/**
 * @file ActuatorSecurityConfig.java
 * @company Techversant Infotech
 * @author Nihal
 * @description Security configuration for Actuator endpoints (health/info)
 */

package com.techversant.accountservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ActuatorSecurityConfig {

    /**
     * Configures a separate security filter chain for Actuator endpoints.
     *
     * @param http the {@link HttpSecurity} object used to configure web-based security for specific HTTP requests
     * @return the configured {@link SecurityFilterChain} for actuator endpoints
     * @throws Exception if an error occurs during security configuration
     */
    @Bean
    @Order(1) // ensure it runs before main security filter chain
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/**") // applies only to actuator endpoints
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) // allow all actuator access
                .csrf(csrf -> csrf.disable()); // disable CSRF for actuator

        return http.build();
    }
}
