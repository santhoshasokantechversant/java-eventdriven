/**
 * @file SecurityConfig.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 27, 2025
 * @version 1.0
 * @description Main security configuration for Customer Service
 */

package com.techversant.customer_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techversant.customer_service.dto.ApiResponse;
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
     * Configures the main {@link SecurityFilterChain} for the application.
     * This security configuration applies to all requests except those handled by
     * the Actuator-specific filter chain
     * Key features:
     *     ensures this filter chain runs after the Actuator security configuration.
     *     CSRF protection is disabled for simplicity in API calls.
     *     Permits unauthenticated access to "/actuator/health" and "/actuator/info".
     *     All other requests require a trusted API Gateway, verified via the "X-Gateway-Auth" header.
     *     Custom exception handling:
     *             Unauthorized (401) responses include a JSON error message when authentication fails.
     *             Forbidden (403) responses include a JSON error message when access is denied.
     * This ensures that only requests from a trusted gateway can access protected endpoints,
     * while providing informative JSON responses for unauthorized or forbidden access.
     *
     * @param http the {@link HttpSecurity} object used to configure web security
     * @return a {@link SecurityFilterChain} enforcing application-wide security rules
     * @throws Exception if an error occurs while building the security filter chain
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
