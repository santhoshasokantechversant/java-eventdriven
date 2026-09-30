/**
 * @file ApiResponse.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 06,2025
 * @version 1.0
 * @description Dto class for api responses
 */

package com.techversant.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String status;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int count;
    public static final String MESSAGE_DATA_FOUND = "Data found.";

    public ApiResponse(String status, String message, T data, int count) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.count = count;
    }

    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
}

