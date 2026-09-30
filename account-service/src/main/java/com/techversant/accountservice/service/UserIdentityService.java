/**
 * @file UserIdentityService.java
 * @company Techversant Infotech
 * @author Siya Elsa Sabu
 * @version 1.0
 * @description UserIdentityService class
 */

package com.techversant.accountservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techversant.accountservice.service.feignclient.UserServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserIdentityService {

    private final UserServiceClient userServiceClient;

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(UserIdentityService.class);

    public UserIdentityService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    /**
     * Retrieves the canonical username either from an external user service or falls back to the token value.
     * The method attempts to resolve the username in the following order:
     * Calls userServiceClient.findUsernameByUserName with the provided token username.
     * If no result is found, calls userServiceClient.findUsernameByEmail with the same value.
     * If both service calls fail or return null, falls back to the original token username.
     * Exceptions from the service client (e.g., Feign client errors) are caught, logged, and
     * the method safely returns the token username.
     *
     * @param usernameFromToken the username extracted from the token (or login input)
     * @return the resolved username from the service or, if unavailable, the original token username
     */
    public String getUsernameFromServiceOrToken(String usernameFromToken) {
        try {
            String usernameFromService = userServiceClient.findUsernameByUserName(usernameFromToken);
            if (usernameFromService == null) {
                usernameFromService = userServiceClient.findUsernameByEmail(usernameFromToken);
            }
            return (usernameFromService != null) ? usernameFromService : usernameFromToken;
        } catch (RuntimeException e) {
            logger.error("Feign client exception: {}", e.getMessage());
            return usernameFromToken;
        }
    }

    /**
     * Retrieves the current username for the request, using a multi-step fallback approach.
     * The resolution order is as follows:
     * Attempt to extract the username from the JWT token in the {@link HttpServletRequest}.
     * If a valid token username is found (not null or "anonymousUser"), attempt to resolve the canonical username
     * via {@link #getUsernameFromServiceOrToken}.
     * If no valid username is available from the token, check the Spring Security authentication context.
     * If a valid authenticated principal is present (and not "anonymousUser"), use its name.
     * If all methods fail, fall back to the default "system" username.
     * <p>
     * Exceptions during username resolution are logged but do not interrupt execution; the method
     * safely returns a username.
     *
     * @param request the {@link HttpServletRequest} containing the JWT token (if available)
     * @return the resolved current username, or "system" if no user is authenticated
     */
    public String getCurrentUsername(HttpServletRequest request) {
        String currentUser = "system";

        try {
            // First try to get username from JWT token
            String usernameFromToken = extractUsernameFromToken(request);

            if (usernameFromToken != null && !usernameFromToken.equals("anonymousUser")) {
                // Use Feign client with the username from token
                currentUser = getUsernameFromServiceOrToken(usernameFromToken);
            } else {
                // Fallback to checking authentication context
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    String usernameFromAuth = authentication.getName();
                    if (!"anonymousUser".equals(usernameFromAuth)) {
                        currentUser = usernameFromAuth;
                    }
                }
            }

        } catch (RuntimeException e) {
            logger.error("Exception in user identity service: {}", e.getMessage());
        }

        return currentUser;
    }

    /**
     * Extracts the username from a JWT token present in the HTTP request's Authorization header.
     * The method performs the following steps:
     * Reads the "Authorization" header from the {@link HttpServletRequest}.
     * Verifies that it starts with "Bearer " and extracts the token string.
     * Decodes the JWT token payload (the second part of the token) using Base64 URL decoder.
     * Parses the payload as JSON using {@link com.fasterxml.jackson.databind.JsonNode}.
     * Attempts to extract the username from multiple possible fields in the JWT:
     * "preferred_username", "sub", "email", or "username".
     * If no username is found or an error occurs, returns null.
     * Exceptions during decoding or parsing are logged but do not interrupt execution.
     *
     * @param request the {@link HttpServletRequest} containing the Authorization header with a JWT
     * @return the extracted username if present; otherwise null
     */
    private String extractUsernameFromToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");


            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // Decode JWT token
                String[] chunks = token.split("\\.");
                if (chunks.length >= 2) {
                    String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]), java.nio.charset.StandardCharsets.UTF_8);

                    JsonNode jsonNode = mapper.readTree(payload);

                    // Try different possible username fields in JWT
                    if (jsonNode.has("preferred_username")) {
                        return jsonNode.get("preferred_username").asText();
                    } else if (jsonNode.has("sub")) {
                        return jsonNode.get("sub").asText();
                    } else if (jsonNode.has("email")) {
                        return jsonNode.get("email").asText();
                    } else if (jsonNode.has("username")) {
                        return jsonNode.get("username").asText();
                    }
                }
            }
        } catch (JsonProcessingException | RuntimeException e) {
            logger.error("Error extracting username from token: {}", e.getMessage());
        }
        return null;
    }
}
