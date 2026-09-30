/**
 * @file UserServiceClient.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description Feign client interface for interacting with the Users Service to delete users and fetch usernames by username or email.
 */

package com.techversant.customer_service.service.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "users-service-client",
        url = "${main.service.url}"  // Define in application.yml or properties
)
public interface UserServiceClient {

    /**
     * Sends a DELETE request to the Users Service to delete the user associated with the given customer ID.
     *
     * @param id the UUID of the customer whose user record needs to be deleted
     */
    @DeleteMapping("/api/v1/users/customer-delete/{id}")
    void deleteUser(@PathVariable("id") UUID id);

    /**
     * Retrieves the username of a user based on the provided username.
     *
     * @param username the username to search for
     * @return the username if found
     */
    @GetMapping("/api/v1/users/find-username-by-username/{username}")
    String findUsernameByUserName(@PathVariable("username") String username);

    /**
     * Retrieves the username associated with the given email address.
     *
     * @param email the email address of the user
     * @return the username corresponding to the provided email
     */
    @GetMapping("/api/v1/users/find-username-by-email/{email}")
    String findUsernameByEmail(@PathVariable("email") String email);
}
