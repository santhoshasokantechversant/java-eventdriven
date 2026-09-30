/**
 * @file EndpointDto.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 11-08-2025
 * @version 1.0
 * @description This Class is a Dto class for Endpoins
 */
package com.techversant.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndpointDto {
    @NotBlank(message = "Endpoint Name is required")
    private String endpointName;
    @NotBlank(message = "Backend url is required")
    private String backendUrl;
    @NotBlank(message = "Http-method is required")
    private String httpMethod;
    @NotBlank(message = "privilege is required")
    private UUID privilegeId;
}
