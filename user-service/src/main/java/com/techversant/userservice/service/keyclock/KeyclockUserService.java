/**
 * @file KeyclockUserService.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 07,2025
 * @version 1.0
 * @description Service class containing various methods for keycloak users
 */

package com.techversant.userservice.service.keyclock;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.techversant.userservice.config.KeycloakAdminConfig;
import com.techversant.userservice.dto.*;
import com.techversant.userservice.model.Role;
import com.techversant.userservice.model.User;
import com.techversant.userservice.repository.RoleRepository;
import com.techversant.userservice.service.feignclient.KeycloakFeignClient;
import com.techversant.userservice.utils.exceptions.FailedToAssignRoleToUser;
import com.techversant.userservice.utils.exceptions.SomethingWentWrongException;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.techversant.userservice.utils.Constants.*;

@Service
public class KeyclockUserService {
    private static final Logger logger = LoggerFactory.getLogger(KeyclockUserService.class);
    private final KeycloakFeignClient keycloakFeignClient;

    private final KeycloakAdminConfig keycloakAdminConfig;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;
    @Value("${keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private final RoleRepository roleRepository;

    public KeyclockUserService(KeycloakAdminConfig keycloakAdminConfig, KeycloakFeignClient keycloakFeignClient, RoleRepository roleRepository) {
        this.keycloakAdminConfig = keycloakAdminConfig;
        this.keycloakFeignClient = keycloakFeignClient;
        this.roleRepository=roleRepository;
    }

    /**
     * Creates a new user in Keycloak and assigns a realm role.
     *
     * @param userDto the data transfer object containing user information and role name
     * @return true if the user is successfully created and assigned a role
     * @throws SomethingWentWrongException if user creation or role assignment fails
     */
    public String createUser(UserDto userDto) {
        Keycloak keycloak = this.keycloakAdminConfig.getInstance();
        CredentialRepresentation credential = new CredentialRepresentation();
        if(userDto.getPasswordHash()!=null){
            credential.setTemporary(false);
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(userDto.getPasswordHash());
        }
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDto.getUserName());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setEnabled(true);
        if(userDto.getPasswordHash()!=null){
            user.setCredentials(Collections.singletonList(credential));
        }
        try {
            Response response = keycloak.realm(realm)
                    .users()
                    .create(user);
            if (response.getStatus() == 201) {
                // Extract userId from response
                String locationPath = response.getLocation().getPath();
                String userId = locationPath.substring(locationPath.lastIndexOf("/") + 1);

                // Assign role
                this.assignRealmRoleToUser(userId, userDto.getRoleName());
                return userId;
            } else {
                throw new SomethingWentWrongException(FAILED_TO_SAVE_USERS_KEYCLOCK);
            }
        } catch (RuntimeException e) {
            throw new SomethingWentWrongException(SOMETHING_WENT_WORNG_KEYCLOCK);
        }
    }


    public void updateUserForRegister(String userId, User user, NewRegisterDto newRegisterDto) {
        Keycloak keycloak = this.keycloakAdminConfig.getInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        try {
            // Get the user by ID
            UserResource userResource = usersResource.get(userId);
            UserRepresentation userRep = userResource.toRepresentation();

            // Update fields (only if provided)
            if (user.getFirstName() != null) userRep.setFirstName(user.getFirstName());
            if (user.getLastName() != null) userRep.setLastName(user.getLastName());
            if (user.getEmail() != null) userRep.setEmail(user.getEmail());
            if (user.getUserName() != null) userRep.setUsername(user.getUserName());

            // Apply update
            userResource.update(userRep);

            // 🔑 Update password if provided
            if (newRegisterDto.getPassword() != null && !newRegisterDto.getPassword().isEmpty()) {
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setTemporary(false);
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(newRegisterDto.getPassword());
                userResource.resetPassword(credential);
            }

        } catch (RuntimeException e) {
            throw new SomethingWentWrongException("Failed to register new user");
        }
    }

    public void assignRoleToUser(String userId, String roleId) {
        Keycloak keycloak = this.keycloakAdminConfig.getInstance();
        UserResource userResource = keycloak.realm(realm).users().get(userId);
        List<RoleRepresentation> currentRoles = userResource.roles()
                .realmLevel()
                .listAll();
        if (!currentRoles.isEmpty()) {
            userResource.roles().realmLevel().remove(currentRoles);
        }
        RoleRepresentation role = keycloak.realm(realm)
                .rolesById()
                .getRole(roleId);

        userResource.roles().realmLevel().add(List.of(role));
    }

    public void deleteUser(String userId) {
        try {
            Keycloak keycloak = this.keycloakAdminConfig.getInstance();
            keycloak.realm(realm)
                    .users()
                    .delete(userId);
            logger.info("User deleted successfully: {}", userId);
        } catch (RuntimeException e) {
            logger.error("Failed to delete user: {}", e.getMessage());
            throw new SomethingWentWrongException("FAILED_TO_DELETE_USER_KEYCLOAK");
        }
    }



    public void updateUserPassword(String userId, UpdatePasswordDto updatePasswordDto) {
        Keycloak keycloak = this.keycloakAdminConfig.getInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        try {
            // Get the user by ID
            UserResource userResource = usersResource.get(userId);
            // 🔑 Update password if provided
            if (updatePasswordDto.getNewPassword() != null && !updatePasswordDto.getNewPassword().isEmpty()) {
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setTemporary(false);
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(updatePasswordDto.getNewPassword());
                userResource.resetPassword(credential);
            }
        } catch (RuntimeException e) {
            throw new SomethingWentWrongException("Failed to update password.");
        }
    }

    public void updateUser(String userId, User user, String roleName) {
        Keycloak keycloak = this.keycloakAdminConfig.getInstance();
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        try {
            UserResource userResource = usersResource.get(userId);
            UserRepresentation userRep = userResource.toRepresentation();

            // Update fields
            if (user.getFirstName() != null) userRep.setFirstName(user.getFirstName());
            if (user.getLastName() != null) userRep.setLastName(user.getLastName());
            if (user.getEmail() != null) userRep.setEmail(user.getEmail());
            if (user.getUserName() != null) userRep.setUsername(user.getUserName());

            // Apply update
            userResource.update(userRep);

            // 🔑 Assign role (replace old with new role)
            if (roleName != null && !roleName.isEmpty()) {
                RoleRepresentation roleRep = realmResource.roles().get(roleName).toRepresentation();
                userResource.roles().realmLevel().add(Collections.singletonList(roleRep));
            }

        } catch (RuntimeException e) {
            logger.error("Keycloak error: {}", e.getMessage());
            throw new SomethingWentWrongException("Failed to update user in Keycloak");
        }
    }





    /**
     * Assigns a realm-level role to a user in Keycloak.
     *
     * @param userId   the unique identifier of the user in Keycloak
     * @param roleName the name of the realm role to assign
     * @return true if the role is successfully assigned
     * @throws FailedToAssignRoleToUser if the role assignment fails due to a runtime exception
     */
    public boolean assignRealmRoleToUser(String userId, String roleName) {
        try {
            Keycloak keycloak = keycloakAdminConfig.getInstance();

            RealmResource realmResource = keycloak.realm(realm);
            UserResource userResource = realmResource.users().get(userId);

            // Get role from Keycloak
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();

            // Assign role to user
            userResource.roles().realmLevel().add(Collections.singletonList(role));
            return true;
        } catch (RuntimeException e) {
            throw new FailedToAssignRoleToUser(FAILED_TO_ASSIGN_ROLE_TO_USER);
        }
    }

    /**
     * Authenticates a user against Keycloak using the Resource Owner Password Credentials (ROPC) grant type.
     *
     * @param loginDto the user's login credentials (email and password hash)
     * @return a TokenDto containing the access and refresh tokens
     * @throws SomethingWentWrongException if an error occurs during the authentication process
     */
    public TokenDto login(LoginDto loginDto) {
        TokenDto token = new TokenDto();
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "password");
            form.add("client_id", clientId);
            form.add("client_secret", clientSecret);
            form.add("username", loginDto.getEmail());
            form.add("password", loginDto.getPasswordHash());
            Map<String, Object> response = keycloakFeignClient.getToken(realm, form);
            token.setValid("ok");
            token.setAccessToken((String) response.get("access_token"));
            token.setRefreshToken((String) response.get("refresh_token"));
            return token;
        } catch (feign.FeignException.Unauthorized e) {
            token.setValid("error");
            token.setMessage("Password is Incorrect");
            return token;
        } catch (feign.FeignException e) {
            token.setValid("error");
            token.setMessage("Something is wrong with key-clock");
            return token;
        } catch (RuntimeException e) {
            token.setValid("error");
            token.setMessage("Something is wrong with key-clock");
            return token;
        }
    }

    public TokenDto refreshToken(String refreshToken) {
        TokenDto token = new TokenDto();
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "refresh_token");
            form.add("client_id", clientId);
            form.add("client_secret", clientSecret);
            form.add("refresh_token", refreshToken);

            Map<String, Object> response = keycloakFeignClient.getToken(realm, form);

            token.setValid("ok");
            token.setAccessToken((String) response.get("access_token"));
            token.setRefreshToken((String) response.get("refresh_token"));
            token.setMessage("Token Refreshed");
            return token;

        } catch (feign.FeignException.Unauthorized e) {
            token.setValid("error");
            token.setMessage("Refresh token expired or invalid");
            return token;
        } catch (feign.FeignException e) {
            token.setValid("error");
            token.setMessage("Something is wrong with Keycloak: " + e.getMessage());
            return token;
        } catch (RuntimeException e) {
            token.setValid("error");
            token.setMessage("Unexpected error: " + e.getMessage());
            return token;
        }
    }

    public boolean logout(String token) {
        try {
            String keycloakLogoutUrl = keycloakServerUrl+"/realms/"+realm+"/protocol/openid-connect/logout";

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", clientId);
            formData.add("refresh_token", token);
            formData.add("client_secret", clientSecret);

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity(keycloakLogoutUrl, formData, String.class);
            return true;
        } catch (RuntimeException e) {
            logger.error("Keycloak logout failed: {}", e.getMessage());
            throw new SomethingWentWrongException("Invalid email or password.");
        }
    }

    /**
     * Decodes a JWT access token to extract user information and roles.
     *
     * @param tokenDto the token data transfer object containing the JWT access token
     * @return a LoginReturnDto populated with user details and roles extracted from the token
     * @throws SomethingWentWrongException if the token decoding fails or required claims are missing
     */
    public LoginReturnDto decode(TokenDto tokenDto) {
        try {
            DecodedJWT jwt = JWT.decode(tokenDto.getAccessToken());
            String username = jwt.getClaim("preferred_username").asString();
            String email = jwt.getClaim("email").asString();
            String firstName = jwt.getClaim("given_name").asString();
            String lastName = jwt.getClaim("family_name").asString();
            List<String> roles = jwt.getClaim("realm_access").asMap() != null
                    ? (List<String>) ((Map<?, ?>) jwt.getClaim("realm_access").asMap()).get("roles")
                    : List.of();
            List<Role> rolesInDatabase=roleRepository.findAllByIsActive(true);
            // Convert DB roles to a Set of role names
            Set<String> dbRoleNames = rolesInDatabase.stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

// Compare with Keycloak roles and keep only common ones
            List<String> commonRoles = roles.stream()
                    .filter(dbRoleNames::contains)
                    .collect(Collectors.toList());

            String role = commonRoles.get(0);
            LoginReturnDto loginResponse = new LoginReturnDto();
            loginResponse.setToken(tokenDto);
            loginResponse.setUserName(username);
            loginResponse.setEmailId(email);
            loginResponse.setRole(role);
            loginResponse.setFullName(firstName + " " + lastName);
            return loginResponse;
        } catch (RuntimeException e) {
            throw new SomethingWentWrongException("Failed to decode token.");
        }

    }

    public void deleteUserByEmail(String email) {
        try {
            Keycloak keycloak = this.keycloakAdminConfig.getInstance();

            // Search for user by email
            List<UserRepresentation> users = keycloak.realm(realm)
                    .users()
                    .search(email, true); // exact match

            if (!users.isEmpty()) {
                String userId = users.get(0).getId();
                keycloak.realm(realm)
                        .users()
                        .delete(userId);
                logger.info("User deleted successfully by email: {}", email);
            } else {
                logger.warn("User not found in Keycloak with email: {}", email);
            }
        } catch (RuntimeException e) {
            logger.error("Failed to delete user by email: {}", e.getMessage());
            throw new SomethingWentWrongException("FAILED_TO_DELETE_USER_BY_EMAIL_KEYCLOAK");
        }
    }

    public void deleteUser(String identifier, boolean isEmail) {
        if (isEmail) {
            deleteUserByEmail(identifier);
        } else {
            deleteUser(identifier); // your existing method
        }
    }
}
