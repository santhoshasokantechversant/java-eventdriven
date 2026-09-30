package com.techversant.userservice.service;

import com.techversant.userservice.dto.EndpointDto;
import com.techversant.userservice.dto.PaginatedEndpointsResponseDto;
import com.techversant.userservice.dto.RolesFilterDto;
import com.techversant.userservice.model.Endpoint;

import java.util.List;
import java.util.UUID;

public interface IEndpointService {

    Endpoint creatEndpoint(EndpointDto endpointDto);

    Endpoint updateEndpoint(UUID id, EndpointDto endpointDto);
    Endpoint deleteEndpoint(UUID id);
    Endpoint getAEndpoint(UUID id);
    PaginatedEndpointsResponseDto listEndpoint(RolesFilterDto rolesFilterDto);
    List<Endpoint> listAllEndpoint();
}
