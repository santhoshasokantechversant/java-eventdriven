package com.techversant.userservice.dto;

import com.techversant.userservice.model.Endpoint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PaginatedEndpointsResponseDto {
    private List<Endpoint> data;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
}
