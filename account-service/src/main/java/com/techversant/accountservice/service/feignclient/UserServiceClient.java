/**
 * @file UserServiceClient.java
 * @company Techversant Infotech
 * @author Siya Elsa Sabu
 * @version 1.0
 * @description Service class used to communicate with other services.
 */

package com.techversant.accountservice.service.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "users-service-client",
        url = "${main.service.url}"  // Define in application.yml or properties
)
public interface UserServiceClient {

    /**
     * Retrieves the username of a user based on the provided username.
     * This endpoint allows clients to query for a user's username by providing
     * the username as a path variable.
     * HTTP Method and URL:
     * GET /api/v1/users/find-username-by-username/{username}
     *
     * @param username the username to search for, provided as a path variable
     * @return the username if found
     */
    @GetMapping("/api/v1/users/find-username-by-username/{username}")
    String findUsernameByUserName(@PathVariable("username") String username);

    /**
     * Retrieves the username associated with the given email address.
     * This endpoint allows clients to query for a user's username by providing
     * the email as a path variable.
     * HTTP Method and URL:
     * GET /api/v1/users/find-username-by-email/{email}
     *
     * @param email the email address of the user to search for, provided as a path variable
     * @return the username corresponding to the provided email
     */
    @GetMapping("/api/v1/users/find-username-by-email/{email}")
    String findUsernameByEmail(@PathVariable("email") String email);
}
