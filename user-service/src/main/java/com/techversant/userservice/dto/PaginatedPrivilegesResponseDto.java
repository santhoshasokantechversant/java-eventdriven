package com.techversant.userservice.dto;

import com.techversant.userservice.model.privileges.PrivilegesNewEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class PaginatedPrivilegesResponseDto {
    private List<PrivilegesNewEntity> data;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
}
