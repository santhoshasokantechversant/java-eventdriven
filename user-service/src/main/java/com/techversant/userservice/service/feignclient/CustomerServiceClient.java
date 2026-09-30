package com.techversant.userservice.service.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "customer-service-client",
        url = "${main.service.url}"  // Define in application.yml or properties
)
public interface CustomerServiceClient {

    @DeleteMapping("/api/v1/customers/email/{email}")
    void deleteCustomerByEmail(@PathVariable("email") String email);
}
