/**
 * @file PrivilegesDto.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 11-08-2025
 * @version 1.0
 * @description This class is for Privilage Dto
 */
package com.techversant.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegesDto {
    private String name;
    private String description;
    private String slugName;
    private String icon;
    private String subName;
    private int position;
    private String url;
}
