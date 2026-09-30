/**
 * @file UserFilterDto.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 06,2025
 * @version 1.0
 * @description Dao for filtering user entities based on different criteria
 */

package com.techversant.userservice.dto;

import com.techversant.userservice.utils.enums.SortDirection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserFilterDto {

    private String firstName = null;
    private String lastName = null;
    private String userName = null;
    private String email = null;
    private UUID roleId = null;
    private int page = 0;
    private int size = 10;
    private String sortField = "createdAt";
    private SortDirection sortDirection = SortDirection.DESC;
}
