/**
 * @file Role.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date August 07,2025
 * @version 1.0
 * @description Entity class for role
 */

package com.techversant.userservice.model;

import com.techversant.userservice.utils.BaseEntity;
import com.techversant.userservice.utils.enums.PermissionType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "roles", schema = "user_schema")
public class Role extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(nullable = false, name = "keyclock_role_id", length=50, unique = true)
    private String keyclockRoleId;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE", name = "is_active")
    private boolean isActive = true;

    @Column(nullable = true, name = "position", length = 50, unique = true)
    private String position;

    @Column(nullable = false, name = "role_create")
    private PermissionType roleCreate;
}
