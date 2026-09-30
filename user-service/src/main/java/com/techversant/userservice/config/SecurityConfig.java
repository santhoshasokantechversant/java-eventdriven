/**
 * @file SecurityConfig.java
 * @company Techversant Infotech
 * @author Nihal
 * @description Main security configuration for User Service
 */

package com.techversant.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techversant.userservice.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    @Order(2) // ✅ runs after actuator config
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/health", "/actuator/info",
                    "/api/v1/users/refresh-token/**",
                    "/api/v1/users/reset-password",
                    "/api/v1/users/get-by-id-encrypted/**",
                    "/api/v1/users/forgot-password-mail-send",
                    "/api/v1/users/login",
                    "/api/v1/users/register-new",
                    "/api/v1/users/create-admin",
                    "/api/v1/users/logout"
                ).permitAll()
                .anyRequest().access((authz, context) -> {
                    String header = context.getRequest().getHeader("X-Gateway-Auth");
                    boolean trusted = "trusted".equals(header);
                    return new org.springframework.security.authorization.AuthorizationDecision(trusted);
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
