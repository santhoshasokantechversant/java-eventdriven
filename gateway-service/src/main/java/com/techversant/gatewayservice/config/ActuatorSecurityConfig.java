/**
 * @file ActuatorSecurityConfig.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description Security configuration for Actuator endpoints (health/info)
 */

package com.techversant.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;

@Configuration
public class ActuatorSecurityConfig {

    /**
     * Configures a security filter chain specifically for Spring Boot actuator endpoints.
     * This bean allows unrestricted access to all actuator endpoints (matching "/actuator/**")
     * and disables CSRF protection for these endpoints. It ensures that monitoring and
     * health-check endpoints are accessible without authentication.
     *
     * @param http the ServerHttpSecurity instance used to configure the filter chain
     * @return a SecurityWebFilterChain configured for actuator endpoints
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) // must run before the JWT chain, or probes get 401
    public SecurityWebFilterChain actuatorSecurityFilterChain(ServerHttpSecurity http) {
        http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/actuator/**"))
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .csrf(ServerHttpSecurity.CsrfSpec::disable);

        return http.build();
    }
}
