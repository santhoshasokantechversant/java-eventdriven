/**
 * @file RoleMapper.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 07,2025
 * @version 1.0
 * @description Mapper class to map role entity to different dto classes and vice versa
 */

package com.techversant.userservice.mapper;

import com.techversant.userservice.dto.PaginatedRoleResponseDto;
import com.techversant.userservice.dto.RoleDto;
import com.techversant.userservice.model.Role;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

@Configuration
public class RoleMapper {

    /**
     * Maps the properties from a RoleDto object to an existing Role entity.
     *
     * @param roleDto the DTO containing updated role data
     * @param role the existing Role entity to be updated
     * @return the updated Role entity with values from the DTO
     */
    public Role roleDtoToEntity(RoleDto roleDto, Role role){
        role.setName(roleDto.getName());
        role.setKeyclockRoleId(roleDto.getKeyclockRoleId());
        role.setDescription(roleDto.getDescription());
        role.setPosition(roleDto.getPosition());
        role.setRoleCreate(roleDto.getRoleCreate());
        return role;
    }

    /**
     * Converts a Page of Role entities into a PaginatedRoleResponseDto.
     *
     * @param roles the paginated list of Role entities
     * @return PaginatedRoleResponseDto containing the role data and pagination metadata
     */
    public PaginatedRoleResponseDto roleToPaginatedRoleResponseDto(Page<Role> roles){
        return new PaginatedRoleResponseDto(
                roles.getContent().stream().toList(),
                roles.getNumber(),
                roles.getTotalPages(),
                (int)roles.getTotalElements(),
                roles.getSize()
        );
    }
}
