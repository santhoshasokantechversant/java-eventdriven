/**
 * @file SecurityConfig.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description Security configuration class
 */

package com.techversant.gatewayservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techversant.common_lib.utility.AuditLogger;
import com.techversant.gatewayservice.dto.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper; // ✅ injected by Spring
    }

    /**
     * Configures the Spring WebFlux Security filter chain for the application.
     * This configuration:
     * - Disables CSRF protection.
     * - Permits all OPTIONS requests for CORS preflight.
     * - Allows unauthenticated access to specific actuator and user endpoints, such as health checks, login, registration, password reset, and token refresh.
     * - Requires authentication for all other requests.
     * - Sets custom authentication entry point and access denied handler for exception handling.
     * - Configures OAuth2 Resource Server support with JWT authentication.
     *
     * @param http the ServerHttpSecurity instance used to configure security
     * @return a SecurityWebFilterChain that applies the defined security rules
     */
    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .pathMatchers("/api/v1/users/refresh-token/**", "/api/v1/users/reset-password", "/api/v1/users/get-by-id-encrypted/**", "/api/v1/users/forgot-password-mail-send", "/api/v1/users/login", "/api/v1/users/register-new", "/api/v1/users/create-admin").permitAll()
                        .anyExchange().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthEntryPoint())
                        .accessDeniedHandler(customAccessDeniedHandler())
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2
                                .jwt(Customizer.withDefaults())   // ✅ new style
                                .authenticationEntryPoint(customAuthEntryPoint())
                                .accessDeniedHandler(customAccessDeniedHandler())
                );

        return http.build();
    }

    /**
     * Defines a custom ServerAccessDeniedHandler for handling forbidden access attempts.
     * This handler:
     * - Logs unauthorized access attempts with user, IP address, and endpoint details.
     * - Sets the HTTP response status to 403 Forbidden.
     * - Returns a JSON response indicating the user does not have permission to access the resource.
     *
     * @return a ServerAccessDeniedHandler that handles access denied exceptions
     */
    @Bean
    public ServerAccessDeniedHandler customAccessDeniedHandler() {
        return (exchange, denied) -> {

            String path = exchange.getRequest().getURI().getPath();
            String ip = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                    .map(addr -> addr.getAddress().getHostAddress())
                    .orElse("unknown");
            String user = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-User-Name"))
                    .orElse("anonymous");

            // ✅ Log unauthorized access attempt
            AuditLogger.log(user, ip, "Security", "FORBIDDEN_ACCESS: " + path, "FAILED");

            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            ApiResponse<Object> response = new ApiResponse<>("error",
                    "You do not have permission to access this resource");

            try {
                byte[] bytes = objectMapper.writeValueAsBytes(response);
                return exchange.getResponse()
                        .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
            } catch (JsonProcessingException ex) {
                return Mono.error(ex);
            }
        };
    }

    /**
     * Defines a custom ServerAuthenticationEntryPoint for handling failed authentication attempts.
     * This entry point:
     * - Logs unauthorized access attempts with user, IP address, and endpoint details.
     * - Sets the HTTP response status to 401 Unauthorized.
     * - Returns a JSON response indicating the token is invalid or expired.
     *
     * @return a ServerAuthenticationEntryPoint that handles authentication failures
     */
    @Bean
    public ServerAuthenticationEntryPoint customAuthEntryPoint() {
        return (exchange, e) -> {

            String path = exchange.getRequest().getURI().getPath();
            String ip = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                    .map(addr -> addr.getAddress().getHostAddress())
                    .orElse("unknown");
            String user = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-User-Name"))
                    .orElse("anonymous");

            // ✅ Log failed authentication
            AuditLogger.log(user, ip, "Security",
                    "UNAUTHORIZED_ACCESS: " + path + " - reason: " + e.getMessage(), "FAILED");

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            String body = """
                    {
                      "status": "error",
                      "message": "Invalid or expired token"
                    }
                    """;

            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)))
                    .then(Mono.fromRunnable(() -> exchange.getResponse().setComplete()));
        };
    }

}
