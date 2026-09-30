/**
 * @file FailedToAssignRoleToUser.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 08,2025
 * @version 1.0
 * @description Exception thrown when assigning a role to a user fails.
 */

package com.techversant.userservice.utils.exceptions;

public class FailedToAssignRoleToUser extends RuntimeException {
    public FailedToAssignRoleToUser(String message) {
        super(message);
    }
}
