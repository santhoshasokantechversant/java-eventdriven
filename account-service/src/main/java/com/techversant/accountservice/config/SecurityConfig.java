/**
 * @file SecurityConfig.java
 * @company Techversant Infotech
 * @author Shajahan M
 * @version 1.0
 * @description Security config file
 */

package com.techversant.accountservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techversant.accountservice.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Defines the main Spring Security filter chain for securing the microservice.
     * This configuration ensures that only requests coming through the API Gateway
     * are allowed to access protected resources. It disables CSRF for stateless APIs,
     * permits health and info endpoints for monitoring, and enforces a custom
     * authorization rule using the "X-Gateway-Auth" header.
     * If the header value equals "trusted", the request is accepted; otherwise,
     * the request is rejected with a structured JSON error response.
     * Custom handlers are defined for both unauthorized and access-denied scenarios
     * to ensure consistent API responses.
     *
     * @param http the HttpSecurity object used to configure web-based security for specific HTTP requests
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during security configuration
     */
    @Bean
    @Order(2) // ✅ Runs after Actuator config
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().access((authz, context) -> {
                            String header = context.getRequest().getHeader("X-Gateway-Auth");
                            boolean trusted = "trusted".equals(header);
                            return new AuthorizationDecision(trusted);
                        })
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            ApiResponse<Object> apiResponse = new ApiResponse<>(
                                    "error",
                                    "Unauthorized access. Only API Gateway can call this service."
                            );
                            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            ApiResponse<Object> apiResponse = new ApiResponse<>(
                                    "error",
                                    "You do not have permission to access this resource directly. Please use API Gateway."
                            );
                            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                        })
                );

        return http.build();
    }
}
