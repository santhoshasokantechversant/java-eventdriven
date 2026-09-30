/**
 * @file EndpointMapper.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 11-08-2025
 * @version 1.0
 * @description This class file is for Endpoint Mapper
 */
package com.techversant.userservice.mapper;

import com.techversant.userservice.dto.EndpointDto;
import com.techversant.userservice.dto.PaginatedEndpointsResponseDto;
import com.techversant.userservice.model.Endpoint;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class EndpointMapper {

    /**
     * Maps an EndpointDto to an existing Endpoint entity.
     *
     * @param endpointDto the data transfer object containing endpoint details
     * @param endpoint    the existing Endpoint entity to be updated
     * @return the updated Endpoint entity with values from the DTO
     */
    public Endpoint endpointDtoToEntity(EndpointDto endpointDto, Endpoint endpoint){
        endpoint.setBackendUrl(endpointDto.getBackendUrl());
        endpoint.setEndponitName(endpointDto.getEndpointName());
        endpoint.setPrivilegeId(endpointDto.getPrivilegeId());
        endpoint.setHttpMethod(endpointDto.getHttpMethod());
        return endpoint;
    }

    public PaginatedEndpointsResponseDto endpointToPaginatedRoleResponseDto(Page<Endpoint> privilegesNewEntities){
        return new PaginatedEndpointsResponseDto(
                privilegesNewEntities.getContent().stream().toList(),
                privilegesNewEntities.getNumber(),
                privilegesNewEntities.getTotalPages(),
                (int)privilegesNewEntities.getTotalElements(),
                privilegesNewEntities.getSize()
        );
    }
}
