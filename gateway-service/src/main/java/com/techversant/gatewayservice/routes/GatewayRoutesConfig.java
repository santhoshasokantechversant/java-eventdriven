/**
 * @file GatewayRoutesConfig.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description Spring Cloud Gateway configuration that defines routes for user, account, customer, and monitoring services with a custom permission filter applied.
 */

package com.techversant.gatewayservice.routes;

import com.techversant.gatewayservice.config.PermissionFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    private final PermissionFilter permissionFilter;
    @Value("${internal.user-service-url}")
    private String userService;

    @Value("${internal.account-service-url}")
    private String accountService;

    @Value("${internal.customer-service-url}")
    private String customerService;

    @Value("${internal.gateway-service-url}")
    private String gatewayService;

    public GatewayRoutesConfig(PermissionFilter permissionFilter) {
        this.permissionFilter = permissionFilter;
    }

    /**
     * Configures custom route mappings for the API Gateway using Spring Cloud Gateway.
     * This RouteLocator:
     * - Routes requests to multiple backend services based on path patterns.
     * - Applies a permissionFilter to handle authorization and other cross-cutting concerns.
     * - Defines routes for:
     * 1. User Service (handles /api/v1/users/**, /api/v1/roles/**, /api/v2/privileges/**, etc.)
     * 2. Account Service (handles /api/v1/account/**, /api/v1/dashboard/**)
     * 3. Customer Service (handles /api/v1/customers/**)
     * 4. Monitoring (handles /actuator/gateway/**)
     *
     * @param builder the RouteLocatorBuilder used to define routes
     * @return a RouteLocator containing the configured routes and filters
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/api/v1/users/**", "/api/v1/roles/**", "/api/v1/privileges-permissions/**", "/api/v2/privileges/**")
                        .filters(f -> f.filter(permissionFilter))
                        .uri(userService))
                .route("account-service", r -> r
                        .path("/api/v1/account/**", "/api/v1/dashboard/**")
                        .filters(f -> f.filter(permissionFilter))
                        .uri(accountService))
                .route("customer-service", r -> r
                        .path("/api/v1/customers/**")
                        .filters(f -> f.filter(permissionFilter))
                        .uri(customerService))
                .route("monitoring", r -> r
                        .path("/actuator/gateway/**")
                        .uri(gatewayService))
                .build();
    }
}
