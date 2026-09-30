/**
 * @file PermissionFilter.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description Custom Spring Cloud Gateway filter that checks user roles and permissions via Keycloak and validates access to backend endpoints before forwarding the request.
 */


package com.techversant.gatewayservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techversant.gatewayservice.utils.exceptions.SomethingWentWrongExceptions;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
@Order(1)
public class PermissionFilter implements GatewayFilter {

    private static final Logger logger = LoggerFactory.getLogger(PermissionFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Keycloak keycloak;
    private final String realm;

    public PermissionFilter(KeycloakAdminConfig keycloakAdminConfig,
                            @Value("${keycloak.realm}") String realm) {
        this.keycloak = keycloakAdminConfig.getInstance();
        this.realm = realm;
    }

    /**
     * Filters incoming requests to the API gateway.
     *
     * <p>This method allows preflight (OPTIONS) requests and certain open endpoints
     * to pass through without authentication. For other requests, it validates the
     * JWT token from the principal and checks permissions. If authentication fails,
     * it returns an error wrapped in a Mono.</p>
     *
     * @param exchange the current server web exchange containing request and response
     * @param chain    the gateway filter chain to delegate to the next filter
     * @return a Mono signaling when request processing is complete, or an error if authentication fails
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        HttpMethod method = exchange.getRequest().getMethod();

        // Allow preflight requests
        if (HttpMethod.OPTIONS.equals(method)) {
            return chain.filter(exchange);
        }

        // Allow specific open endpoints
        if (path.startsWith("/api/v1/users/get-by-id-encrypted/") ||
                path.startsWith("/api/v1/users/reset-password") ||
                path.startsWith("/api/v1/users/refresh-token/") ||
                path.contains("/forgot-password-mail-send") ||
                path.contains("/login") ||
                path.contains("/create-admin") ||
                path.contains("/register-new") ||
                path.contains("/logout")) {
            return chain.filter(exchange);
        }

        // Validate token safely
        return exchange.getPrincipal()
                .flatMap(principal -> {
                    if (principal instanceof JwtAuthenticationToken auth) {
                        return processPermissions(exchange, chain, auth, path);
                    } else {
                        // Not a JWT, unauthorized
                        return Mono.error(new SomethingWentWrongExceptions("Authentication required"));
                    }
                })
                .onErrorResume(throwable -> {
                    logger.error("Permission check failed for {}", path, throwable);
                    return Mono.error(new SomethingWentWrongExceptions("Authentication failed: " + throwable.getMessage()));
                });
    }

    /**
     * Processes permissions for a request based on the JWT token and user roles.
     *
     * <p>This method extracts roles from the JWT token, fetches role-based permissions
     * from Keycloak (except for admin roles which are granted full access), and checks
     * if the user is authorized to access the requested endpoint and HTTP method.
     * If authorized, it forwards the request with additional headers containing user
     * information and roles. Otherwise, it returns an error.</p>
     *
     * @param exchange the current server web exchange containing request and response
     * @param chain    the gateway filter chain to delegate to the next filter
     * @param auth     the JwtAuthenticationToken containing the JWT claims
     * @param path     the requested endpoint path
     * @return a Mono signaling when request processing is complete, or an error if unauthorized
     */
    private Mono<Void> processPermissions(ServerWebExchange exchange,
                                          GatewayFilterChain chain,
                                          JwtAuthenticationToken auth,
                                          String path) {
        var jwt = auth.getToken();

        // Extract roles from token
        List<String> roles = Optional.ofNullable((Map<String, Object>) jwt.getClaims().get("realm_access"))
                .map(map -> (List<String>) map.get("roles"))
                .orElse(List.of());

        if (roles.isEmpty()) {
            return Mono.error(new SomethingWentWrongExceptions("Invalid token: no roles found"));
        }

        List<Map<String, String>> permissions = new ArrayList<>();
        boolean isAdmin = false;

        for (String roleName : roles) {
            try {
                if ("Admin".equalsIgnoreCase(roleName) || "Asst Manager 1".equalsIgnoreCase(roleName)) {
                    isAdmin = true; // Admin shortcut
                    continue;       // skip fetching from Keycloak
                }

                RoleRepresentation role = keycloak.realm(realm)
                        .roles()
                        .get(roleName)
                        .toRepresentation();

                Map<String, List<String>> attributes = role.getAttributes();
                List<String> rolePermissions = attributes.getOrDefault("permissions", Collections.emptyList());

                for (String perm : rolePermissions) {
                    Map<String, String> permMap = objectMapper.readValue(perm, Map.class);
                    permissions.add(permMap);
                }

            } catch (JsonProcessingException | RuntimeException e) {
                logger.error("Failed to load permissions for role {}", roleName, e);
                return Mono.error(new SomethingWentWrongExceptions("Role fetch failed for " + roleName + ": " + e.getMessage()));
            }
        }

        if (permissions.isEmpty() && !isAdmin) {
            return Mono.error(new SomethingWentWrongExceptions("You are not authorized: no permissions found"));
        }

        String requestMethod = exchange.getRequest().getMethod().name();
        String endpoint = path;
        boolean allowed = isAdmin
                || permissions.stream()
                // Check first for non-null endpointName
                .filter(p -> p.get("endpointName") != null)
                .anyMatch(p -> matchPermission(p, endpoint, requestMethod))
                || permissions.stream()
                // Then check for null endpointName
                .filter(p -> p.get("endpointName") == null)
                .anyMatch(p -> matchPermission(p, endpoint, requestMethod));

        if (!allowed) {
            return Mono.error(new SomethingWentWrongExceptions("You are not authorized for this endpoint"));
        }

        // Forward JWT and add custom headers
        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header("X-Gateway-Auth", "trusted")
                .header("Authorization", "Bearer " + jwt.getTokenValue())
                .header("X-User-Id", jwt.getClaimAsString("sub"))
                .header("X-User-Email", jwt.getClaimAsString("email"))
                .header("X-User-Name", jwt.getClaimAsString("preferred_username"))
                .header("X-User-Roles", String.join(",", roles))
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        return chain.filter(mutatedExchange);
    }

    /**
     * Checks if a given permission entry matches the requested endpoint and HTTP method.
     *
     * <p>This method validates that the permission map contains a backend URL, HTTP method,
     * and permission type. It then checks whether the requested endpoint contains the
     * backend URL, the request method matches the permission method, and the permission
     * type is "YES".</p>
     *
     * @param p             the permission map containing keys "backendUrl", "httpMethod", and "permissionType"
     * @param endpoint      the requested endpoint path
     * @param requestMethod the HTTP method of the request (GET, POST, etc.)
     * @return true if the permission allows access to the endpoint with the given method, false otherwise
     */
    private boolean matchPermission(Map<String, String> p, String endpoint, String requestMethod) {
        String permEndpoint = p.get("backendUrl");
        String permMethod = p.get("httpMethod");
        String permValue = p.get("permissionType");
        return permEndpoint != null && permMethod != null && permValue != null
                && endpoint.toLowerCase().contains(permEndpoint.toLowerCase())
                && requestMethod.equalsIgnoreCase(permMethod)
                && "YES".equalsIgnoreCase(permValue);
    }
}
