/**
 * @file UserIdentityService.java
 * @company Techversant Infotech
 * @author Siya Elsa Sabu
 * @version 1.0
 * @description Service that resolves the current user's username from the JWT token or authentication context, with fallback to the user service.
 */

package com.techversant.customer_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techversant.customer_service.service.feignclient.UserServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserIdentityService {

    private final UserServiceClient userServiceClient;

    public UserIdentityService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    private static final ObjectMapper mapper = new ObjectMapper();
    Logger logger = LoggerFactory.getLogger(UserIdentityService.class);

    /**
     * Retrieves the current username of the authenticated user.
     * This method attempts to extract the username from the JWT token in the
     * HttpServletRequest. If the token is not available or contains an anonymous
     * user, it falls back to the Spring Security authentication context. If no
     * valid username is found, it defaults to "system".
     *
     * @param request the HttpServletRequest containing potential JWT token
     * @return the username of the currently authenticated user, or "system" if not available
     */
    public String getCurrentUsername(HttpServletRequest request) {
        String currentUser = "system";

        try {
            // First try to get username from JWT token
            String usernameFromToken = extractUsernameFromToken(request);

            if (usernameFromToken != null && !"anonymousUser".equals(usernameFromToken)) {
                currentUser = getUsernameFromService(usernameFromToken);
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
     * Retrieves the canonical username from the User Service.
     * This method attempts to resolve the username using the User Service
     * client. It first tries to find the username by the provided username,
     * and if not found, it attempts to find it by email. If neither succeeds,
     * it returns the original input. Any exceptions during the call are logged
     * and the original input is returned.
     *
     * @param usernameFromToken the username extracted from the JWT token
     * @return the canonical username as returned by the User Service, or the original input if not found
     */
    private String getUsernameFromService(String usernameFromToken) {
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
     * Extracts the username from a JWT token present in the Authorization header.
     * This method decodes the JWT token from the "Authorization" header of the
     * provided HttpServletRequest and attempts to extract the username from
     * common fields such as "preferred_username", "sub", "email", or "username".
     *
     * @param request the HttpServletRequest containing the Authorization header
     * @return the extracted username if available; otherwise, returns null
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
