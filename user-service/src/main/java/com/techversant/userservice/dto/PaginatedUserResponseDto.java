/**
 * @file PaginatedUserResponseDto.java
 * @company Techversant Infotech
 * @author Nipin J George
 * @date August 06,2025
 * @version 1.0
 * @description Dto class for get all users api
 */

package com.techversant.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedUserResponseDto {
    private List<UserDisplayDto> data;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
}
