/**
 * @file SideNavMapper.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 22-08-2025
 * @version 1.0
 * @description This calss is for side-nav mapper
 */
package com.techversant.userservice.mapper;

import com.techversant.userservice.dto.PrivilegesDto;
import com.techversant.userservice.model.SideNav;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SideNavMapper {
    public SideNav sideNavDtoToEntity(PrivilegesDto privilegesDto, SideNav sideNav, SideNav subPrivilege) {
        sideNav.setIcon(privilegesDto.getIcon());
        sideNav.setPosition(privilegesDto.getPosition());
        sideNav.setSlugName(privilegesDto.getSlugName());
        sideNav.setName(privilegesDto.getName());
        if(privilegesDto.getSubName() != null && !privilegesDto.getSubName().equals("")) {
            sideNav.setParent(subPrivilege);
        }
        return sideNav;
    }
}
