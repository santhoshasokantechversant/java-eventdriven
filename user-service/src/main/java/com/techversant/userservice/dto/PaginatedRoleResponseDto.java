/**
 * @file PaginatedRoleResponseDto.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 08,2025
 * @version 1.0
 * @description Dto class for get all roles api
 */

package com.techversant.userservice.dto;

import com.techversant.userservice.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedRoleResponseDto {
    private List<Role> data;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
}
