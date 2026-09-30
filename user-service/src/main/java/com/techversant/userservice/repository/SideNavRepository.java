package com.techversant.userservice.repository;

import com.techversant.userservice.model.SideNav;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * @author Nihal Elton John
 * @version 1.0
 * @file SideNavRepository.java
 * @company Techversant Infotech
 * @date 22-08-2025
 * @description SideNav interface repository
 */

public interface SideNavRepository extends JpaRepository<SideNav, UUID> {
    List<SideNav> findAllByIsActive(boolean isActive);
    SideNav findByIdAndIsActive(UUID id, boolean isActive);

    List<SideNav> findByParentIsNull();
    List<SideNav> findByParentIsNullOrderByPositionAsc();
}
