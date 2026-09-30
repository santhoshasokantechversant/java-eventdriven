/**
 * @file KeyclockRoleService.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 07,2025
 * @version 1.0
 * @description Service class containing various methods for keycloak roles
 */

package com.techversant.userservice.service.keyclock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techversant.userservice.config.KeycloakAdminConfig;
import com.techversant.userservice.dto.PermissionKeyclockDto;
import com.techversant.userservice.model.Role;
import com.techversant.userservice.model.privileges.PermissionsNewEntity;
import com.techversant.userservice.utils.exceptions.DeleteRoleFailedException;
import com.techversant.userservice.utils.exceptions.KeycloakRoleUpdateException;
import com.techversant.userservice.utils.exceptions.PermissionSerializationException;
import com.techversant.userservice.utils.exceptions.SomethingWentWrongException;
import org.keycloak.admin.client.Keycloak;
import com.techversant.userservice.dto.RoleDto;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.techversant.userservice.utils.Constants.*;

@Service
public class KeyclockRoleService {

    private final KeycloakAdminConfig keycloakAdminConfig;
    @Value("${keycloak.realm}")
    private String realm;

    private static final Logger logger = LoggerFactory.getLogger(KeyclockRoleService.class);

    public KeyclockRoleService(KeycloakAdminConfig keycloakAdminConfig) {
        this.keycloakAdminConfig = keycloakAdminConfig;
    }

    /**
     * Creates a new role in Keycloak.
     *
     * @param roleDto the role data containing name and description
     * @return true if the role was successfully created in Keycloak
     * @throws SomethingWentWrongException if role creation fails in Keycloak
     */
    public String createRole(RoleDto roleDto) {
        Keycloak keycloak = this.keycloakAdminConfig.getInstance();
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleDto.getName());
        role.setDescription(roleDto.getDescription());
        role.setComposite(false);
        try {
            // Step 1: Create role in Keycloak
            keycloak.realm(realm).roles().create(role);

            // Step 2: Fetch newly created role by name
            RoleRepresentation createdRole = keycloak.realm(realm)
                    .roles()
                    .get(roleDto.getName())
                    .toRepresentation();

            return createdRole.getId();
        } catch (RuntimeException e) {
            logger.error("Keycloak role operation failed", e);
            throw new SomethingWentWrongException(FAILED_TO_SAVE_ROLE_KEYCLOCK);
        }
    }

    /**
     * Updates a Keycloak role with the given permissions.
     *
     * @param roleId                   the ID of the Keycloak role to update
     * @param permissionKeyclockDtoArr a list of PermissionKeyclockDto objects representing the updated permissions
     * @throws RuntimeException if JSON serialization of any permission fails or if Keycloak update operations encounter issues
     */
    public void updateRole(String roleId, List<PermissionKeyclockDto> permissionKeyclockDtoArr) {
        Keycloak keycloak = this.keycloakAdminConfig.getInstance();
        RealmResource realmResource = keycloak.realm(realm);

        // Get role resource by ID
        RoleRepresentation existingRole = realmResource.rolesById().getRole(roleId);

        // Convert permissions to JSON strings
        List<String> permissionsJson = permissionKeyclockDtoArr.stream()
                .map(permission -> {
                    try {
                        return new ObjectMapper().writeValueAsString(permission);
                    } catch (JsonProcessingException e) {
                        throw new PermissionSerializationException(  "Failed to serialize permission for role ID: " ,e);
                    }
                })
                .toList();

        // 🔥 Clear ALL existing attributes first
        Map<String, List<String>> attributes = new HashMap<>();

        // Add only the new "permissions" attribute
        attributes.put(PERMISSIONS, permissionsJson);

        existingRole.setAttributes(attributes);

        // Update the role in Keycloak
        RoleResource roleResource = realmResource.roles().get(existingRole.getName());
        roleResource.update(existingRole);
    }

    public void updateNewAttributeRole(String roleId, List<PermissionKeyclockDto> permissionKeycloakDtoArr) {
        Keycloak keycloak = this.keycloakAdminConfig.getInstance();
        RealmResource realmResource = keycloak.realm(realm);

        // Get role resource by ID
        RoleRepresentation existingRole = realmResource.rolesById().getRole(roleId);

        // Convert new permissions to JSON strings
        List<String> permissionsJson = permissionKeycloakDtoArr.stream()
                .map(permission -> {
                    try {
                        return new ObjectMapper().writeValueAsString(permission);
                    } catch (JsonProcessingException e) {
                        throw new PermissionSerializationException("Failed to serialize permission for role ID: " ,e);
                    }
                })
                .toList();

        // ✅ Preserve existing attributes
        Map<String, List<String>> attributes = existingRole.getAttributes();
        if (attributes == null) {
            attributes = new HashMap<>();
        }

        // ✅ Merge with existing "permissions"
        List<String> existingPermissions = attributes.getOrDefault(PERMISSIONS, new ArrayList<>());
        existingPermissions = new ArrayList<>(existingPermissions); // make modifiable
        existingPermissions.addAll(permissionsJson);

        attributes.put(PERMISSIONS, existingPermissions);

        // Set updated attributes back
        existingRole.setAttributes(attributes);

        // Update the role in Keycloak
        RoleResource roleResource = realmResource.roles().get(existingRole.getName());
        roleResource.update(existingRole);
    }

    public void updateRealmRole(String keycloakRoleId, Role roleData) {
        try {
            Keycloak keycloak = this.keycloakAdminConfig.getInstance();

            // Get the role by its Keycloak ID
            RoleRepresentation role = keycloak.realm(realm)
                    .rolesById()
                    .getRole(keycloakRoleId);

            // Update fields directly
            role.setName(roleData.getName());
            role.setDescription(roleData.getDescription());

            // Update role in Keycloak
            keycloak.realm(realm)
                    .rolesById()
                    .updateRole(keycloakRoleId, role);

        } catch (RuntimeException e) {
            throw new KeycloakRoleUpdateException("Failed to update Keycloak role with ID: ",e);
        }
    }




    public void deleteRealmRole(String roleId) {
        Keycloak keycloak = this.keycloakAdminConfig.getInstance();
        try {
            keycloak.realm(realm)
                    .rolesById()
                    .deleteRole(roleId);  // ✅ Deletes role by ID
        } catch (RuntimeException e) {
            throw new DeleteRoleFailedException("Failed to delete role with ID: " + roleId, e);
        }
    }


    /**
     * Replaces all existing attributes in Keycloak for each role
     * with only the permissions related to that specific role.
     *
     * @param roles                    list of Role entities (contains keycloakRoleId)
     * @param allPermissions           list of all PermissionsNewEntity from DB
     */

    public void replaceAllAttributesForRoles(List<Role> roles, List<PermissionsNewEntity> allPermissions) {
        Keycloak keycloak = this.keycloakAdminConfig.getInstance();
        RealmResource realmResource = keycloak.realm(realm);
        ObjectMapper mapper = new ObjectMapper();

        for (Role role : roles) {
            try {
                String keycloakRoleId = role.getKeyclockRoleId();

                if (keycloakRoleId == null || keycloakRoleId.isEmpty()) {
                    logger.error("Skipping role because keycloakRoleId is null for: {}", role.getName());
                }

                // Filter permissions specific to this role
                List<PermissionsNewEntity> rolePermissions = allPermissions.stream()
                        .filter(permission -> permission.getRoleId() != null
                                && permission.getRoleId().equals(role.getId()))
                        .toList();

                if (rolePermissions.isEmpty()) {
                    logger.warn("No permissions found for role: {}", role.getName());
                }
                List<String> permissionsJson = rolePermissions.stream()
                        .map(permission -> {
                            try {
                                Map<String, Object> data = new HashMap<>();
                                data.put("backendUrl", permission.getBackendUrl());
                                data.put("httpMethod", permission.getHttpMethod());
                                data.put("privilegeName", permission.getPrivilegeName());
                                data.put("permissionType", permission.getPermissionType());
                                data.put("endpointName", permission.getEndpointName());
                                return mapper.writeValueAsString(data);
                            } catch (JsonProcessingException e) {
                                throw new PermissionSerializationException("Failed to serialize permission map for ID: " + permission.getId(), e);
                            }
                        })
                        .toList();

                // Set Keycloak role attributes
                Map<String, List<String>> newAttributes = new HashMap<>();
                newAttributes.put("permissions", permissionsJson);

                RoleRepresentation existingRole = realmResource.rolesById().getRole(keycloakRoleId);

                if (existingRole == null) {
                    logger.error("Keycloak role not found for ID: {} ({})", keycloakRoleId, role.getName());
                    continue;
                }

                existingRole.setAttributes(newAttributes);

                RoleResource roleResource = realmResource.roles().get(existingRole.getName());
                roleResource.update(existingRole);

            } catch (RuntimeException e) {
                logger.error("Keycloak role operation failed", e);
                throw new SomethingWentWrongException(
                        "Failed to update Keycloak attributes for role: " + role.getName() + " | " + e.getMessage());
            }
        }
    }

public void replaceAttributesForRole(Role role, List<PermissionsNewEntity> allPermissions) {
    Keycloak keycloak = this.keycloakAdminConfig.getInstance();
    RealmResource realmResource = keycloak.realm(realm);
    ObjectMapper mapper = new ObjectMapper();

    try {
        String keycloakRoleId = role.getKeyclockRoleId();

        if (keycloakRoleId == null || keycloakRoleId.isEmpty()) {
            logger.warn("⚠️ Skipping role because keycloakRoleId is null for: {}", role.getName());
            return;
        }

        // Filter only permissions for this role
        List<PermissionsNewEntity> rolePermissions = allPermissions.stream()
                .filter(permission -> permission.getRoleId() != null
                        && permission.getRoleId().equals(role.getId()))
                .toList();

        if (rolePermissions.isEmpty()) {
            logger.warn("⚠️ No permissions found for role: {}", role.getName());
            return; // nothing to update
        }

        // ✅ Safe serialization using a clean map
        List<String> permissionsJson = rolePermissions.stream()
                .map(permission -> {
                    try {
                        Map<String, Object> data = new HashMap<>();
                        data.put("backendUrl", permission.getBackendUrl());
                        data.put("httpMethod", permission.getHttpMethod());
                        data.put("privilegeName", permission.getPrivilegeName());
                        data.put("permissionType", permission.getPermissionType());
                        data.put("endpointName", permission.getEndpointName());
                        return mapper.writeValueAsString(data);
                    } catch (JsonProcessingException e) {
                        throw new PermissionSerializationException("Failed to serialize permission map for ID: " + permission.getId(), e);
                    }
                })
                .toList();

        // Prepare the new attributes map
        Map<String, List<String>> newAttributes = new HashMap<>();
        newAttributes.put("permissions", permissionsJson);

        // Fetch the existing Keycloak role
        RoleResource roleResource = realmResource.roles().get(role.getName());
        RoleRepresentation existingRole = roleResource.toRepresentation();

        if (existingRole == null) {
            logger.warn("⚠️ Keycloak role not found for: {}", role.getName());
            return;
        }

        // Replace attributes (existing attributes will be overwritten)
        existingRole.setAttributes(newAttributes);
        roleResource.update(existingRole);

    } catch (RuntimeException e) {
        logger.error("Keycloak role operation failed", e);
        throw new SomethingWentWrongException(
                "Failed to update Keycloak attributes for role: " + role.getName() + " | " + e.getMessage());
    }
}


}
