/**
 * @file UserMapper.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 05,2025
 * @version 1.0
 * @description Mapper class to map user entity to different dto classes and vice versa
 */

package com.techversant.userservice.mapper;

import com.techversant.userservice.dto.PaginatedUserResponseDto;
import com.techversant.userservice.dto.UserDisplayDto;
import com.techversant.userservice.dto.UserDto;
import com.techversant.userservice.model.Role;
import com.techversant.userservice.model.User;
import com.techversant.userservice.model.UserRole;
import com.techversant.userservice.utils.enums.PermissionType;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserMapper {

    /**
     * Maps data from a  UserDto object to a User entity.
     *
     * @param userDto the source DTO containing user data
     * @param user    the target User entity to be updated
     * @return the updated User entity with data copied from the UserDto
     */
    public User userDtoToEntity(UserDto userDto, User user) {
        user.setEmail(userDto.getEmail());
        user.setUserName(userDto.getUserName());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setCity(userDto.getCity());
        user.setAddress(userDto.getAddress());
        user.setCountry(userDto.getCountry());
        user.setState(userDto.getState());
        user.setPostalCode(userDto.getPostalCode());
        user.setDateOfBirth(userDto.getDateOfBirth());
        user.setUserNo(userDto.getUserNo());
        user.setUserType(userDto.getUserType());
        user.setRoleId(UUID.fromString(userDto.getRoleId()));
        if (userDto.getStaticUser() != null) {
            user.setStaticUser(userDto.getStaticUser());
        }
        user.setRegistered(PermissionType.NO);
        if (userDto.getRegistered() != null && userDto.getRegistered().equals(PermissionType.YES)) {
            user.setRegistered(PermissionType.YES);
        }
        user.setKeyclockUserId(userDto.getKeyclockUserId());
        return user;
    }

    /**
     * Converts a Page of User entities into a  PaginatedUserResponseDto containing pagination metadata
     * and a list of  UserDisplayDto objects for display purposes.
     *
     * @param users the Page of User entities to be converted
     * @return the populated PaginatedUserResponseDto containing user display data and pagination details
     */
    public PaginatedUserResponseDto userToPaginatedUserResponseDto(Page<User> users) {
        PaginatedUserResponseDto paginatedUserResponseDto = new PaginatedUserResponseDto();
        paginatedUserResponseDto.setCurrentPage(users.getNumber());
        paginatedUserResponseDto.setPageSize(users.getSize());
        paginatedUserResponseDto.setTotalItems(users.getTotalElements());
        paginatedUserResponseDto.setTotalPages(users.getTotalPages());
        paginatedUserResponseDto.setData(
                users.getContent().stream()
                        .map(
                                user ->
                                        new UserDisplayDto(
                                                user.getId(),
                                                user.getUserName(),
                                                user.getEmail(),
                                                user.getFirstName(),
                                                user.getLastName(),
                                                user.getCreatedAt(),
                                                user.getUpdatedAt(),
                                                user.getUserNo(),
                                                user.getDateOfBirth(),
                                                user.getAddress(),
                                                user.getCity(),
                                                user.getState(),
                                                user.getPostalCode(),
                                                user.getCountry(),
                                                user.getPhoneNumber(),
                                                user.isActive(),
                                                user.getRoleId()
                                        )

                        ).toList()
        );
        return paginatedUserResponseDto;
    }

    /**
     * Converts a User entity to a UserDisplayDto for response purposes.
     *
     * @param user the User entity to be converted
     * @return a UserDisplayDto containing the mapped user information
     */
    public UserDisplayDto userEntityToUserDisplayDto(User user) {
        return new UserDisplayDto(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getUserNo(),
                user.getDateOfBirth(),
                user.getAddress(),
                user.getCity(),
                user.getState(),
                user.getPostalCode(),
                user.getCountry(),
                user.getPhoneNumber(),
                user.isActive(),
                user.getRoleId()
        );
    }

    /**
     * Maps User and Role entities to an existing UserRole entity.
     *
     * @param user     the User entity to be associated with the UserRole
     * @param role     the Role entity to be associated with the UserRole
     * @param userRole the existing UserRole entity to be updated
     * @return the updated UserRole entity with assigned user and role
     */
    public UserRole userRoleDtoToEntity(User user, Role role, UserRole userRole) {
        userRole.setRole(role);
        userRole.setUser(user);
        return userRole;
    }
}
