/**
 * @file SidenavResponseDto.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 25-08-2025
 * @version 1.0
 * @description this class is for to return side nav
 */
package com.techversant.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SidenavResponseDto {
    private String id;
    private String moduleName;
    private String slugName;
    private String icon;
    private int position;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SidenavResponseDto> subPrivileges = new ArrayList<>();
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> access;
}
