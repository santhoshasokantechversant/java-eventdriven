/**
 * @file RolesFilterDto.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 08,2025
 * @version 1.0
 * @description Dao for filtering role entities based on name
 */

package com.techversant.userservice.dto;

import com.techversant.userservice.utils.enums.SortDirection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RolesFilterDto {
    private String name = null;
    private int page = 0;
    private int size = 10;
    private String sortField = "createdAt";
    private SortDirection sortDirection = SortDirection.DESC;
}
