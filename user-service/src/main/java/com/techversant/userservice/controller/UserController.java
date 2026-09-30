/**
 * @file UserController.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 05,2025
 * @version 1.0
 * @description Controller to handle all apis related to user
 */

package com.techversant.userservice.controller;

import com.google.gson.JsonObject;
import com.techversant.common_lib.events.UserDetailsDto;
import com.techversant.common_lib.utility.AuditLogger;
import com.techversant.userservice.dto.*;
import com.techversant.userservice.model.User;
import com.techversant.userservice.repository.UserRepository;
import com.techversant.userservice.service.IUserService;
import com.techversant.userservice.utils.enums.SortDirection;
import com.techversant.userservice.utils.exceptions.SomethingWentWrongException;
import com.techversant.userservice.utils.exceptions.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.techversant.userservice.utils.Constants.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final IUserService iUserService;
    private final UserRepository userRepository;

    public UserController(IUserService iUserService, UserRepository userRepository) {
        this.iUserService = iUserService;
        this.userRepository = userRepository;
    }



    @PostMapping("/create-admin")
    public ResponseEntity<ApiResponse<User>> createAdmin() {
        User user = this.iUserService.createAdmin();
        ApiResponse<User> apiResponse = new ApiResponse<>();
        if (user.getId() == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("Failed to create Admin");
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Admin created successfully.");
        apiResponse.setData(user);
        return ResponseEntity.ok(apiResponse);
    }


    @PostMapping("/add-user")
    public ResponseEntity<ApiResponse<User>> addUser(
            @Valid @RequestBody UserDto userDto,
            HttpServletRequest request) {

        String currentUser =iUserService.getCurrentAuthenticatedUsername();
        String clientIp = request.getRemoteAddr();

        final String userId = currentUser;
        final String userIp = clientIp;

        try {
            User user = this.iUserService.addUser(userDto);
            ApiResponse<User> apiResponse = new ApiResponse<>();

            if (user.getId() == null) {
                apiResponse.setStatus(STATUS_ERROR);
                apiResponse.setMessage(USER_CREATED_FAILED);

                // AUDIT LOG: User creation failed
                AuditLogger.log(
                        userId,                                   // user
                        userIp,                                   // ip
                        "User",                                   // entity
                        "ADD_USER",                               // action
                        STATUS_FAILED                  // status
                );

                return ResponseEntity.ok(apiResponse);
            }

            apiResponse.setStatus(STATUS_SUCCESS);
            apiResponse.setMessage(USER_CREATED);
            apiResponse.setData(user);

            //AUDIT LOG: User created successfully
            AuditLogger.log(
                    userId,                                   // user
                    userIp,                                   // ip
                    "User",                                   // entity
                    "ADD_USER: " + user.getId(),              // action with user ID
                    STATUS_SUCCESS                 // status
            );

            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            //AUDIT LOG: User creation exception
            AuditLogger.log(
                    userId,                                   // user
                    userIp,                                   // ip
                    "User",                                   // entity
                    "ADD_USER - Error: " + e.getMessage(),    // action with error
                    "ERROR"                                   // status
            );

            ApiResponse<User> apiResponse = new ApiResponse<>();
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("User creation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Handles HTTP GET requests to retrieve a paginated list of users.
     *
     * @param page          the page number to retrieve (default is 0)
     * @param size          the number of users per page (default is 10)
     * @param sortField     the field by which to sort the results (default is "firstName")
     * @param sortDirection the direction of sorting: ASC for ascending or DESC for descending (default is ASC)
     * @return a ResponseEntity containing an ApiResponse with status, message, and paginated user data
     */
    @GetMapping("/get-all-users")
    public ResponseEntity<ApiResponse<PaginatedUserResponseDto>> getAllUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "createdAt") String sortField, @RequestParam(defaultValue = "DESC") SortDirection sortDirection) {
        Sort sort = sortDirection == SortDirection.ASC ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        PaginatedUserResponseDto users = this.iUserService.getAllUsers(PageRequest.of(page, size, sort));
        ApiResponse<PaginatedUserResponseDto> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(USERS_RETRIEVED);
        apiResponse.setData(users);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Handles HTTP GET requests to retrieve a user by their unique ID.
     *
     * @param id the UUID of the user to retrieve
     * @return a ResponseEntity containing an ApiResponse with the user data or an error message
     */
    @GetMapping("/get-user-by-id/{id}")
    public ResponseEntity<ApiResponse<UserDisplayDto>> getUserById(@PathVariable UUID id) {
        UserDisplayDto userDisplayDto = iUserService.getUserById(id);
        ApiResponse<UserDisplayDto> apiResponse = new ApiResponse<>();
        if (userDisplayDto == null) {
            throw new UserNotFoundException(USER_NOT_FOUND);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(USER_RETRIEVED);
        apiResponse.setData(userDisplayDto);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Handles HTTP GET requests to filter users based on provided filter criteria.
     *
     * @param userFilterDto the DTO containing filter and pagination criteria such as firstName, lastName, userName, email, page, size, sortField and sortDirection.
     * @return a ResponseEntity containing an ApiResponse with the filtered and paginated list of users
     */
    @GetMapping("/filter-users")
    public ResponseEntity<ApiResponse<PaginatedUserResponseDto>> filterUsers(@ModelAttribute UserFilterDto userFilterDto) {
        PaginatedUserResponseDto users = iUserService.filterUsers(userFilterDto);
        ApiResponse<PaginatedUserResponseDto> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(USERS_RETRIEVED);
        apiResponse.setData(users);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Handles HTTP PUT requests to update an existing user's information.
     *
     * @param id            the UUID of the user to update
     * @param userUpdateDto the request body containing the fields to be updated
     * @return a  ResponseEntity containing an ApiResponse with the updated
     * UserDisplayDto, success status, and message
     */


    @PutMapping("/update-user/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable UUID id,
            @RequestBody UserUpdateDto userUpdateDto,
            HttpServletRequest request) {

        String currentUser = iUserService.getCurrentAuthenticatedUsername();
        String clientIp = request.getRemoteAddr();

        final String userId = currentUser;
        final String userIp = clientIp;

        try {
            User userDisplayDto = iUserService.updateUser(id, userUpdateDto);
            ApiResponse<User> apiResponse = new ApiResponse<>();
            apiResponse.setStatus(STATUS_SUCCESS);
            apiResponse.setMessage(USER_UPDATED);
            apiResponse.setData(userDisplayDto);

            //AUDIT LOG: User updated successfully
            AuditLogger.log(
                    userId,                                   // user
                    userIp,                                   // ip
                    "User",                                   // entity
                    "UPDATE_USER: " + id,                     // action with user ID
                    "SUCCESS"                                 // status
            );

            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            //AUDIT LOG: User update exception
            AuditLogger.log(
                    userId,                                   // user
                    userIp,                                   // ip
                    "User",                                   // entity
                    "UPDATE_USER: " + id + " - Error: " + e.getMessage(), // action with error
                    STATUS_FAILED                     // status
            );

            ApiResponse<User> apiResponse = new ApiResponse<>();
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("User update failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PutMapping("/update-new-password/{id}")
    public ResponseEntity<ApiResponse<User>> updateNewpassword(@PathVariable UUID id, @RequestBody UpdatePasswordDto userUpdateDto) {
        User userDisplayDto = iUserService.updateUserPassword(id, userUpdateDto);
        ApiResponse<User> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(USER_UPDATED);
        apiResponse.setData(userDisplayDto);
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/update-user-role/{id}")
    public ResponseEntity<ApiResponse<User>> updateUserRole(@PathVariable UUID id, @RequestBody UpdateUserRole updateUserRole) {
        User userDisplayDto = iUserService.updateUserRole(id, updateUserRole);
        ApiResponse<User> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(USER_UPDATED);
        apiResponse.setData(userDisplayDto);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Handles HTTP DELETE requests to delete a user by their UUID.
     *
     * @param id the UUID of the user to be deleted
     * @return a ResponseEntity containing an ApiResponse with the operation status and message
     */


    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<ApiResponse<JsonObject>> deleteUser(
            @PathVariable UUID id,
            HttpServletRequest request) {

        String currentUser = iUserService.getCurrentAuthenticatedUsername();
        String clientIp = request.getRemoteAddr();

        final String userId = currentUser;
        final String userIp = clientIp;

        try {
            // AUDIT LOG: Before calling service (attempting deletion)
            AuditLogger.log(
                    userId,                                   // user
                    userIp,                                   // ip
                    "User",                                   // entity
                    USER_DELETED +id,          // action with user ID
                    "ATTEMPT"                                 // status
            );

            ResponseEntity<ApiResponse<JsonObject>> response = iUserService.deleteUser(id);
            ApiResponse<JsonObject> body = response.getBody();

            // Check if the service response indicates success
            if (response.getStatusCode().is2xxSuccessful() &&
                    body != null &&
                    STATUS_SUCCESS.equals(body.getStatus())) {

                // AUDIT LOG: User deleted successfully
                AuditLogger.log(
                        userId,                                   // user
                        userIp,                                   // ip
                        "User",                                   // entity
                        USER_DELETED + id,         // action with user ID
                        "SUCCESS"                                 // status
                );
            } else {
                // AUDIT LOG: User deletion failed based on service response
                AuditLogger.log(
                        userId,                                   // user
                        userIp,                                   // ip
                        "User",                                   // entity
                        "DELETE_USER: " + id + " - Service returned failure", // action
                        "FAILED"                                  // status
                );
            }

            return response;

        } catch (RuntimeException e) {
            // AUDIT LOG: User deletion exception
            AuditLogger.log(
                    userId,                                   // user
                    userIp,                                   // ip
                    "User",                                   // entity
                    "DELETE_USER: " + id + " - Error: " + e.getMessage(), // action with error
                    "FAILED"                                  // status
            );

            // Re-throw to preserve original behavior, or return error response
            throw e;
        }
    }

    /**
     * Endpoint for user login.
     *
     * @param loginDto the data transfer object containing user login credentials
     * @return ResponseEntity containing ApiResponse with success or error status, message, and user information if login is successful
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginReturnDto>> logIn(@Valid @RequestBody LoginDto loginDto) {
        LoginReturnDto userRes = iUserService.login(loginDto);
        ApiResponse<LoginReturnDto> apiResponse = new ApiResponse<>();
        if (userRes.getUserName().isEmpty() || userRes.getUserName().isBlank()) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage(LOGIN_FAILED);
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage(LOGIN_SUCCESS);
        apiResponse.setData(userRes);
        return ResponseEntity.ok(apiResponse);

    }

    @PostMapping("/register-new")
    public ResponseEntity<ApiResponse<User>> registerNew(@Valid @RequestBody NewRegisterDto newRegisterDto) {
        User user = iUserService.registerNew(newRegisterDto);
        ApiResponse<User> apiResponse = new ApiResponse<>();
        if (user.getUserName().isEmpty() || user.getUserName().isBlank()) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("Failed to Register a Account.");
            return ResponseEntity.ok(apiResponse);
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Successfully Register a Account.");
        apiResponse.setData(user);
        return ResponseEntity.ok(apiResponse);

    }

    @PostMapping("/check-exist-customer")
    public ResponseEntity<ApiResponse<Boolean>> checkCustomerExists(@Valid @RequestBody UserDetailsDto userDetailsDto) {
        boolean canCreate = iUserService.checkCustomerExists(userDetailsDto);

        ApiResponse<Boolean> apiResponse = new ApiResponse<>();

        if (canCreate) {
            apiResponse.setStatus(STATUS_SUCCESS);
            apiResponse.setMessage("Can continue...");
            apiResponse.setData(true);
        } else {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("Email or phone number already exists.");
            apiResponse.setData(false);
        }

        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/users-count")
    public ResponseEntity<ApiResponse<Integer>> usersCount() {
        try {
            // Get all active users
            List<User> activeUsers = iUserService.getUsersCount();
            int count = activeUsers.size();

            ApiResponse<Integer> apiResponse = new ApiResponse<>();
            apiResponse.setStatus(STATUS_SUCCESS);
            apiResponse.setMessage("Total active users count");
            apiResponse.setData(count);
            apiResponse.setCount(count);
            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            throw new SomethingWentWrongException("Failed to fetch active user count", e);
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Boolean>> logout(@RequestParam String refreshToken) {
        try {
            boolean result = iUserService.logout(refreshToken);

            ApiResponse<Boolean> apiResponse = new ApiResponse<>();
            apiResponse.setData(result);

            if (!result) {
                apiResponse.setStatus(STATUS_ERROR);
                apiResponse.setMessage("Failed to logout.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse); // Reflects failure
            }

            apiResponse.setStatus(STATUS_SUCCESS);
            apiResponse.setMessage("Logout successful.");
            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            throw new SomethingWentWrongException("Failed to logout.", e);
        }
    }


    @DeleteMapping("/customer-delete/{id}")
    public ResponseEntity<Void> deleteUserByCustomer(@PathVariable UUID id) {
        iUserService.deleteUserByCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password-mail-send")
    public ResponseEntity<ApiResponse<JsonObject>> forgotPasswordMailSend(@Valid @RequestBody ForgetPasswordDto forgetPasswordDto ) {
        return  iUserService.forgotPasswordMailSend(forgetPasswordDto);
    }

    @GetMapping("/get-by-id-encrypted/{id}")
    public ResponseEntity<ApiResponse<UserEncryptedReturnDto>> getUserByEncrypted(@PathVariable String id) {
        UserEncryptedReturnDto user = iUserService.getUserByEncrypted(id);
        ApiResponse<UserEncryptedReturnDto> apiResponse = new ApiResponse<>();
        if(user.getEmail() == null ) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("Failed to Logout.");
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Successfully user fetched.");
        apiResponse.setData(user);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<User>> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        User log = iUserService.resetPassword(resetPasswordDto);
        ApiResponse<User> apiResponse = new ApiResponse<>();
        if(log == null) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("Failed to Reset Password.");
        }
        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Password Successfully Reset.");
        return ResponseEntity.ok(apiResponse);
    }
    @PostMapping("/refresh-token/{refreshToken}")
    public ResponseEntity<ApiResponse<TokenDto>> refreshToken(@PathVariable String refreshToken) {
        TokenDto token = this.iUserService.refreshToken(refreshToken);
        ApiResponse<TokenDto> apiResponse = new ApiResponse<>();

        if (token == null || "error".equals(token.getValid())) {
            apiResponse.setStatus(STATUS_ERROR);
            apiResponse.setMessage("Failed to refresh token.");
            return ResponseEntity.ok(apiResponse);
        }

        apiResponse.setStatus(STATUS_SUCCESS);
        apiResponse.setMessage("Token refreshed successfully.");
        apiResponse.setData(token);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/find-username-by-username/{username}")
    public String findUsernameByUserName(@PathVariable String username) {
        User user = userRepository.findByUserName(username);
        return (user != null && user.getId() != null) ? user.getUserName() : null;
    }

    @GetMapping("/find-username-by-email/{email}")
    public String findUsernameByEmail(@PathVariable String email) {
        User user = userRepository.findByEmail(email);
        return (user != null && user.getId() != null) ? user.getUserName() : null;
    }

}
