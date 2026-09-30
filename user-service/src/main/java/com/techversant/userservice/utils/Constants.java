/**
 * @file Constants.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 06,2025
 * @version 1.0
 * @description Class containing constant values
 */

package com.techversant.userservice.utils;

import org.springframework.stereotype.Component;

@Component
public class Constants {
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";
    public static final String STATUS = "status";
    public static final String MESSAGE = "message";
    public static final String USER_CREATED = "User created successfully.";
    public static final String USER_CREATED_FAILED = "Failed to create User.";
    public static final String USER_ALREADY_EXISTS = "User Name already Exists.";
    public static final String EMAIL_ALREADY_EXISTS = "Email Id already Exists.";
    public static final String FAILED_TO_SAVE_USERS_KEYCLOCK = "Failed to Save Users in Key-clock.";
    public static final String SOMETHING_WENT_WORNG_KEYCLOCK = "Something Went wrong with Key-clock.";
    public static final String FAILED_TO_SAVE_ROLE_KEYCLOCK = "Failed to Save Role in Key-clock.";
    public static final String USER_NOT_FOUND = "User not found.";
    public static final String USERS_RETRIEVED = "Users retrieved successfully.";
    public static final String USER_RETRIEVED = "User with specified id retrieved successfully.";
    public static final String USER_UPDATED = "User updated successfully.";
    public static final String USER_UPDATE_FAILED="Failed to update user.";
    public static final String FIRST_NAME_NOT_BLANK_IF_PROVIDED = "First name should not be blank if provided.";
    public static final String LAST_NAME_NOT_BLANK_IF_PROVIDED = "Last name should not be blank if provided.";
    public static final String USER_DELETED="User deleted successfully.";
    public static final String USER_DELETE_FAILED="Failed to delete user.";
    // FOR ROLE
    public static final String ROLE_CREATED = "Role created successfully.";
    public static final String ROLE_CREATED_FAILED = "Failed to create Role.";
    public static final String ROLES_RETRIEVED="All roles retrieved successfully.";
    public static final String ROLE_NOT_FOUND="Role not found.";
    public static final String ROLE_RETRIEVED="Role with specified id retrieved successfully.";
    public static final String ROLE_DELETED="Role deleted successfully.";
    public static final String ROLE_DELETE_FAILED="Failed to delete role.";
    public static final String ROLE_NAME_NOT_BLANK_IF_PROVIDED="Role name should not be blank if provided.";
    public static final String ROLE_DESCRIPTION_NOT_BLANK_IF_PROVIDED="Role description should not be blank if provided.";
    public static final String ROLE_UPDATE_FAILED="Failed to update role.";
    public static final String ROLE_UPDATED="Role updated successfully";
    public static final String FAILED_TO_ASSIGN_ROLE_TO_USER = "Failed to assign role to user";

    public static final String PRIVILEGE_ALREADY_EXISTS = "Privilege Name already Exists.";
    public static final String PRIVILEGE_CREATED = "Privilege created successfully.";
    public static final String PRIVILEGE_CREATED_FAILED = "Failed to create Privilege.";
    public static final String PRIVILEGE_NOT_FOUND = "Privilege not found.";

    public static final String ENDPOINT_ALREADY_EXISTS = "Endpoint Name already Exists.";
    public static final String HTTP_METHODS_ALREADY_EXISTS = "Http Method already Exists.";
    public static final String ENDPOINT_URL_ALREADY_EXISTS = "Endpoint url already Exists.";
    public static final String ENDPOINT_CREATED = "Endpoint created successfully.";
    public static final String ENDPOINT_CREATED_FAILED = "Failed to create Endpoint.";
    public static final String ENDPOINT_NOT_FOUND = "Endpoint not found.";
    public static final String INVALID_USER_ID = "Invalid user id.";
    public static final String INVALID_ROLE_ID = "Invalid role id.";
    public static final String INVALID_PRIVILEGE_ID = "Invalid privilege id.";
    public static final String INVALID_ENDPOINT_ID = "Invalid privilege id.";

    public static final String SIDENAV_NOT_FOUND="Side-nav not found.";
    public static final String INVALID_USER_ROLE_MESSAGE = "Invalid user role.";
    public static final String USER_ROLES_HEADER = "X-User-Roles";
    public static final String RESET_PASSWORD = "Reset Password";
    public static final String ERROR_FAILED_TO_DECRYPT = "failed to decrypt";
    public static final String STATUS_FAILED = "failed";
    public static final String USER_LOGIN = "User Login";
    public static final String FORGOT_PASSWORD = "ForgotPassword";
    public static final String INVALIDEMORUN = "Invalid email or username";
    public static final String PERMISSIONS = "Permissions";


    // Log-IN
    public static final String LOGIN_SUCCESS = "Log-in successfully.";
    public static final String LOGIN_FAILED = "Log-in failed. Please check the mail-id and password.";
    public static final String DELETEUSER = "DELETE_USER: ";
    private Constants() {

    }
}
