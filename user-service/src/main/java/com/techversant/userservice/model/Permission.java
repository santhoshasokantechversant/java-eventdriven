/**
 * @file Permission.java
 * @company Techversant Infotech
 * @author Nihal Elton John
 * @date 11-08-2025
 * @version 1.0
 * @description This Class is an entity class for Permission
 */
package com.techversant.userservice.model;

import com.techversant.userservice.utils.BaseEntity;
import com.techversant.userservice.utils.enums.PermissionType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "permission", schema = "user_schema")
public class Permission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    @ManyToOne
    @JoinColumn(name = "privilege_id", nullable = false)
    private Privileges privileges;
    @ManyToOne
    @JoinColumn(name = "endpoint_id", nullable = false)
    private Endpoint endpoint;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false)
    private PermissionType permissionType;
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE", name = "is_active")
    private boolean isActive = true;
    @Column(name = "slug_name", nullable = true)
    private String slugName;
    @Enumerated(EnumType.STRING)
    @Column(name = "side_nav", nullable = false)
    private PermissionType sideNav = PermissionType.NO;
}
