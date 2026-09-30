/**
 * @file PrivilegesMapper.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 11-08-2025
 * @version 1.0
 * @description Mapper class for Privilage
 */
package com.techversant.userservice.mapper;

import com.techversant.userservice.dto.PaginatedPrivilegesResponseDto;
import com.techversant.userservice.dto.PrivilegesDto;
import com.techversant.userservice.model.Privileges;
import com.techversant.userservice.model.privileges.PrivilegesNewEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PrivilegesMapper {

    /**
     * Maps a PrivilegesDto to an existing Privileges entity.
     *
     * @param privilegesDto the data transfer object containing privilege details
     * @param privileges    the existing Privileges entity to be updated
     * @return the updated Privileges entity with values from the DTO
     */
    public Privileges privilegeDtoToEntity(PrivilegesDto privilegesDto, Privileges privileges){
        privileges.setName(privilegesDto.getName());
        privileges.setDescription(privilegesDto.getDescription());
        privileges.setUrl(privilegesDto.getUrl());
        return privileges;
    }

    public PaginatedPrivilegesResponseDto privilegeToPaginatedRoleResponseDto(Page<PrivilegesNewEntity> privilegesNewEntities){
        return new PaginatedPrivilegesResponseDto(
                privilegesNewEntities.getContent().stream().toList(),
                privilegesNewEntities.getNumber(),
                privilegesNewEntities.getTotalPages(),
                (int)privilegesNewEntities.getTotalElements(),
                privilegesNewEntities.getSize()
        );
    }
}
