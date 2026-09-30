/**
 * @file AccountsServiceClient.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @version 1.0
 * @description Feign client interface for communicating with the Accounts Service to perform account-related operations.
 */

package com.techversant.customer_service.service.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "accounts-service-client",
        url = "${main.service.url}"  // Define in application.yml or properties
)
public interface AccountsServiceClient {

    /**
     * Sends a DELETE request to the Accounts Service to delete the account associated with the given customer ID.
     *
     * @param id the UUID of the customer whose account needs to be deleted
     */
    @DeleteMapping("/api/v1/account/customer-delete/{id}")
    void deleteAccount(@PathVariable("id") UUID id);
}
