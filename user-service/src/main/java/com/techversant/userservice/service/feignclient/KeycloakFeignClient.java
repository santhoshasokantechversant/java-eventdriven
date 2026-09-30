/**
 * @file KeycloakFeignClient.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 08,2025
 * @version 1.0
 * @description Class for communicating with Keycloak
 */

package com.techversant.userservice.service.feignclient;

import com.techversant.userservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(
        name = "keycloak-client",
        url = "${keycloak.server-url}",
        configuration = FeignConfig.class
)
public interface KeycloakFeignClient {

    @PostMapping(
            value = "/realms/{realm}/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    Map<String, Object> getToken(
            @PathVariable("realm") String realm,
            @RequestBody MultiValueMap<String, String> form
    );

}
