/**
 * @file IUserService.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 05,2025
 * @version 1.0
 * @description Service interface for handling operations related to user entity
 */

package com.techversant.userservice.service;

import com.google.gson.JsonObject;
import com.techversant.common_lib.events.UserDetailsDto;
import com.techversant.userservice.dto.*;
import com.techversant.userservice.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface IUserService {

    /**
     * Adds a new user based on the provided UserDto.
     *
     * @param userDto the data transfer object containing user details
     * @return the created User entity
     */
    User addUser(UserDto userDto);

    User createAdmin();

    /**
     * Retrieves a paginated list of all active users.
     *
     * @param pageable the pagination and sorting information
     * @return a PaginatedUserResponseDto containing the list of users and pagination metadata
     */
    PaginatedUserResponseDto getAllUsers(Pageable pageable);

    /**
     * Retrieves an active user by their unique ID and returns the data as a UserDisplayDto.
     *
     * @param id the UUID of the user to retrieve
     * @return a UserDisplayDto containing the user's information if found and active
     */
    UserDisplayDto getUserById(UUID id);

    /**
     * Filters and retrieves a paginated list of active users based on the provided filter criteria.
     *
     * @param userFilterDto the DTO containing filtering conditions, pagination, and sorting preferences
     * @return a PaginatedUserResponseDto containing the filtered user data and pagination metadata
     */
    PaginatedUserResponseDto filterUsers(UserFilterDto userFilterDto);

    /**
     * Updates the details of an existing user identified by the given UUID.
     *
     * @param id            the UUID of the user to update
     * @param userUpdateDto a DTO containing the fields to be updated
     * @return a UserDisplayDto containing the updated user information
     */
    User updateUser(UUID id, UserUpdateDto userUpdateDto);


    User updateUserPassword(UUID id, UpdatePasswordDto userUpdateDto);

    User updateUserRole(UUID id, UpdateUserRole updateUserRole);

    /**
     * Deletes a user identified by the given UUID.
     *
     * @param id the UUID of the user to be deleted
     * @return a ResponseEntity containing an ApiResponse with the result of the delete operation
     */
    ResponseEntity<ApiResponse<JsonObject>> deleteUser(UUID id);

    /**
     * Authenticates a user with the provided login credentials.
     *
     * @param loginDto the login data transfer object containing user credentials
     * @return a LoginReturnDto containing user details and authentication tokens
     */
    LoginReturnDto login(LoginDto loginDto);

    User registerNew(NewRegisterDto newRegisterDto);
    boolean checkCustomerExists(UserDetailsDto userDetailsDto);
    List<User> getUsersCount();
    boolean logout(String token);
    void deleteUserByCustomer(UUID id);
    ResponseEntity<ApiResponse<JsonObject>> forgotPasswordMailSend(ForgetPasswordDto forgetPasswordDto );
    UserEncryptedReturnDto getUserByEncrypted(String id);
    User resetPassword(ResetPasswordDto resetPasswordDto);
    TokenDto refreshToken(String refreshToken);
    String getCurrentAuthenticatedUsername();
    void sendEmail(User user, String role);
}
