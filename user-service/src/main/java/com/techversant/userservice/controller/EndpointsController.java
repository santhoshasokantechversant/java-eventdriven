package com.techversant.userservice.controller;

import com.techversant.userservice.dto.ApiResponse;
import com.techversant.userservice.dto.EndpointDto;
import com.techversant.userservice.dto.PaginatedEndpointsResponseDto;
import com.techversant.userservice.dto.RolesFilterDto;
import com.techversant.userservice.model.Endpoint;
import com.techversant.userservice.service.IEndpointService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.techversant.userservice.utils.Constants.STATUS_ERROR;
import static com.techversant.userservice.utils.Constants.STATUS_SUCCESS;

@RestController
@RequestMapping("/api/v2/privileges/endpoints")
public class EndpointsController {
    private final IEndpointService iEndpointService;

    public EndpointsController(IEndpointService iEndpointService) {
        this.iEndpointService = iEndpointService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Endpoint>> creatEndpoint(@RequestBody EndpointDto endpointDto) {
        Endpoint endpoint = this.iEndpointService.creatEndpoint(endpointDto);
        ApiResponse<Endpoint> apiResponse = new ApiResponse<>();
        if (endpoint.getId() == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("Failed to create End-point");
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("End-point created successfully.");
        apiResponse.setData(endpoint);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Endpoint>> updateEndpoint(@PathVariable UUID id, @RequestBody EndpointDto endpointDto) {
        Endpoint endpoint = this.iEndpointService.updateEndpoint(id, endpointDto);
        ApiResponse<Endpoint> apiResponse = new ApiResponse<>();
        if (endpoint.getId() == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("Failed to update End-point");
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("End-point updated successfully.");
        apiResponse.setData(endpoint);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Endpoint>> deleteEndpoint(@PathVariable UUID id) {
        Endpoint endpoint = this.iEndpointService.deleteEndpoint(id);
        ApiResponse<Endpoint> apiResponse = new ApiResponse<>();
        if (endpoint.getId() == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("Failed to delete End-point");
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("End-point deleted successfully.");
        apiResponse.setData(endpoint);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Endpoint>> getAEndpoint(@PathVariable UUID id) {
        Endpoint endpoint = this.iEndpointService.getAEndpoint(id);
        ApiResponse<Endpoint> apiResponse = new ApiResponse<>();
        if (endpoint.getId() == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("No data found for the given ID.");
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(ApiResponse.MESSAGE_DATA_FOUND);
        apiResponse.setData(endpoint);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/filter-list")
    public ResponseEntity<ApiResponse<PaginatedEndpointsResponseDto>> listEndpoint(@ModelAttribute RolesFilterDto rolesFilterDto) {
        PaginatedEndpointsResponseDto endpoint = this.iEndpointService.listEndpoint(rolesFilterDto);
        ApiResponse<PaginatedEndpointsResponseDto> apiResponse = new ApiResponse<>();
        if (endpoint == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("No data found.");
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Data found.");
        apiResponse.setData(endpoint);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/list-all")
    public ResponseEntity<ApiResponse<List<Endpoint>>> listAllEndpoint() {
        List<Endpoint> endpoints = this.iEndpointService.listAllEndpoint();

        ApiResponse<List<Endpoint>> apiResponse = new ApiResponse<>();
        if (endpoints == null || endpoints.isEmpty()) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("No data found.");
            return ResponseEntity.ok(apiResponse);
        }

        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Data found.");
        apiResponse.setData(endpoints);
        return ResponseEntity.ok(apiResponse);
    }


}
