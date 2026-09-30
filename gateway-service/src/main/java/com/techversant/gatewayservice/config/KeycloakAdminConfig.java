/**
 * @file KeycloakAdminConfig.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 07,2025
 * @version 1.0
 * @description Configuration file for keycloak
 */

package com.techversant.gatewayservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

@Configuration
public class KeycloakAdminConfig {
    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.admin.realm}")
    private String adminRealm;

    /**
     * Creates and returns a Keycloak admin client instance.
     * This method builds a Keycloak instance using the configured server URL,
     * admin realm, username, password, and the "admin-cli" client ID. It can
     * be used to interact with the Keycloak server for administrative operations.
     *
     * @return a Keycloak instance configured for admin operations
     */
    public Keycloak getInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(adminRealm)
                .username(adminUsername)
                .password(adminPassword)
                .clientId("admin-cli")
                .build();
    }
}

